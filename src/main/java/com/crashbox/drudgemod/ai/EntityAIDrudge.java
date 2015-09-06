package com.crashbox.drudgemod.ai;

import com.crashbox.drudgemod.DrudgeUtils;
import com.crashbox.drudgemod.EntityDrudge;
import com.crashbox.drudgemod.messaging.*;
import com.crashbox.drudgemod.task.*;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.Sys;

import java.util.*;
import java.util.concurrent.LinkedTransferQueue;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class EntityAIDrudge extends EntityAIBase implements IMessager
{
    public EntityAIDrudge(EntityDrudge entity)
    {
        this(entity, DEFAULT_SPEED, DEFAULT_RANGE);
        Broadcaster.getInstance().subscribe(Broadcaster.Channel.RED, new MyListener());
    }

    public EntityAIDrudge(EntityDrudge entity, double speed, int range)
    {
        this._entity = entity;
    }

    public EntityDrudge getEntity()
    {
        return _entity;
    }

    @Override
    public boolean shouldExecute()
    {
        updateTask();
        return (_currentTask != null);
    }

    @Override
    public void startExecuting()
    {
        // The update task loop starts it
//        _currentTask = null;
//        _requestEndMS = System.currentTimeMillis() + REQUEST_TIMEOUT_MS;
//        _state = State.ELICITING;
//        Broadcaster.postMessage(new MessageWorkerAvailability(_entity.worldObj, this), _channel);
    }

    @Override
    public boolean continueExecuting()
    {
        return _state != State.IDLING;
    }

    @Override
    public void resetTask()
    {
        startExecuting();
    }

    @Override
    public void updateTask()
    {
        processMessages();
        //LOGGER.debug("UpdateTask: " + _state);
        switch (_state)
        {
            case IDLING:
                _state = idle();
                break;
            case ELICITING:
                _state = elicit();
                break;
            case TRANSITING:
                _state = transition();
                break;
            case TARGETING:
                _state = target();
                break;
            case PERFORMING:
                _state = perform();
                break;
        }
    }

    // ================
    // IMessager

    @Override
    public BlockPos getPos()
    {
        return getEntity().getPosition();
    }

    @Override
    public int getRadius()
    {
        return 3;
    }

    //=============================================================================================

    private void processMessages()
    {
        Message msg;
        while ((msg = _messages.poll()) != null)
        {
            // If we sent it, skip it
            if (msg.getSender() == this)
                continue;

            // Skip ones intended for someone else.
            if (msg.getTarget() != null && msg.getTarget() != this)
                continue;

            // Filter all task requests
            if (msg instanceof MessageTaskRequest && _currentTask == null)
            {
                if (msg.getCause() == MessageWorkerAvailability.class)
                    _proposedTasks.add(_taskFactory.makeTaskFromMessage(this, (MessageTaskRequest) msg));
                else
                    _responseTasks.add((MessageTaskRequest)msg);
            }
            else if (msg.getCause() != null)
            {
                // If it has a 'cause' it is a response to something we sent before.
                _responses.add(msg);
            }
            else
            {
                // Handle immediately

                // Process it
                LOGGER.debug("Unhandled message: " + msg);
            }
        }
    }

    //=============================================================================================
    // IDLING

    private State idle()
    {
        // Once in a while we want to tell people we need more
        if (System.currentTimeMillis() > _nextElicit )
        {
            LOGGER.debug("Idle timeout over.");
            _nextElicit = System.currentTimeMillis() + CHECK_DELAY_MILLIS;
            _requestEndMS = System.currentTimeMillis() + REQUEST_TIMEOUT_MS;
            Broadcaster.postMessage(new MessageWorkerAvailability(_entity.worldObj, this), _channel);
            return State.ELICITING;
        }
        return State.IDLING;
    }


    //=============================================================================================
    // ELICITING

    private State elicit()
    {
        // If we have some tasks to link, then let's do that
        linkupResponses(_responseTasks);

        // Try to resolve to the issue back messages
        resolveAllTasks();

        // IMPROVEMENT:  We could stop accepting new ones and only process resolves.  This
        // could shorten the time we wait.  So have two timeouts.  New starter task arrival
        // and entire elicitation arrival.

        // When we hit the timeout we are done.
        if (System.currentTimeMillis() > _requestEndMS)
        {
            _currentTask = selectNextTask();
            _proposedTasks.clear();

            if (_currentTask == null)
            {
                return State.IDLING;
            }

            return State.TRANSITING;
        }

        // Ask for a new task
        return State.ELICITING;
    }

    private void linkupResponses(List<MessageTaskRequest> responses)
    {
        for (int x = 0; x < _proposedTasks.size(); ++x)
        {
            List<MessageTaskRequest> taskResponses = getAllForTask(responses, _proposedTasks.get(x));
            if (taskResponses.size() > 0)
            {
                MessageTaskRequest opt = findBestResponseOption(taskResponses);
                TaskBase newTask = _taskFactory.makeTaskFromMessage(this, (MessageTaskRequest) opt);
                newTask.setNextTask(_proposedTasks.get(x));
                _proposedTasks.set(x, newTask);
            }

            if (responses.size() == 0)
                return;
        }

        for (MessageTaskRequest response : responses)
        {
            LOGGER.debug(response);
        }
    }

    private List<MessageTaskRequest> getAllForTask(List<MessageTaskRequest> responses, TaskBase task)
    {
        List<MessageTaskRequest> result = new ArrayList<MessageTaskRequest>();

        Iterator<MessageTaskRequest> iterator = responses.iterator();
        while (iterator.hasNext())
        {
            MessageTaskRequest msg =  iterator.next();
            if (msg.getCause() == task)
            {
                iterator.remove();
                result.add(msg);
            }
        }

        return result;
    }

    private MessageTaskRequest findBestResponseOption(List<MessageTaskRequest> messages)
    {
        // For now just find the first
        return messages.get(0);
    }

    private void resolveAllTasks()
    {
        for (TaskBase task : _proposedTasks)
        {
            if (task.getResolving() == TaskBase.Resolving.UNRESOLVED)
            {
                LOGGER.debug("Resolving: " + task);
                // Get a new message send it out
                Message msg = task.resolve();
                if (msg != null)
                {
                    Broadcaster.postMessage(msg, _channel);
                }
            }
        }
    }

    private TaskBase selectNextTask()
    {
        int bestValue = Integer.MIN_VALUE;
        TaskBase bestTask = null;

        for (TaskBase task : _proposedTasks)
        {
            // If unresolved (has pre-reqs) then skip it
            if (task.getResolving() == TaskBase.Resolving.RESOLVED)
            {
                int value = getTaskValue(task);
                if (value > bestValue)
                {
                    bestValue = value;
                    bestTask = task;
                }
            }
        }

        // Find highest resolved.
        return bestTask;
    }

    private int getTaskValue(TaskBase task)
    {
        // The cost is the transit time (distance) currently linear for each one
        // added in its inherent cost.
        BlockPos startPos = getEntity().getPosition();
        int cost = computeDistanceCost(startPos, task) + task.getValue();
        while (task.getNextTask() != null)
        {
            startPos = task.getRequester().getPos();
            task = task.getNextTask();
            cost += computeDistanceCost(startPos, task) + task.getValue();
        }

        return cost;
    }

    private int computeDistanceCost(BlockPos startPos, TaskBase task)
    {
        return (int) Math.sqrt(startPos.distanceSq(task.getRequester().getPos()));
    }



    //=============================================================================================
    // TRANSITIONING

    // In this function we transition to the target site. It might be far away.
    private State transition()
    {
        // If within 20, then issue location request and to start targeting
        if (posInAreaXY(getPos(), _currentTask.getRequester().getPos(), 20))
        {
            LOGGER.debug("Within distance, switching to targeting.");
            requestWorkAreas();
            _requestEndMS = System.currentTimeMillis() + REQUEST_TIMEOUT_MS;
            return State.TARGETING;
        }
        else if (!getEntity().getNavigator().noPath())
        {
            LOGGER.debug("Couldn't get a path during transition, idling");
            // If we have no path, then we are done.
            resetTask();
            return State.IDLING;
        }

        return State.TRANSITING;
    }

    //=============================================================================================
    // TARGETING

    private State target()
    {
        // Collect work area messages from other bots
        extractWorkAreas();

        // After some period of time, get the task top generate a workArea based on its specifics
        if (_workArea == null && System.currentTimeMillis() > _requestEndMS)
        {
            _workArea = _currentTask.selectWorkArea(_workAreas);
            if (_workArea == null)
            {
                LOGGER.debug("Failed to find work area, aborting. " + _currentTask);
                _currentTask = null;
                return State.IDLING;
            }

            LOGGER.debug("Determining work area and redirecting: " + _workArea + " currently at: " + getPos());
            tryMoveTo(_workArea);
        }

        // If we have no path, then we are done.
        if (_workArea != null && getEntity().getNavigator().noPath())
        {
            if (inProximity(_workArea))
            {
                return State.PERFORMING;
            }
            else
            {
                LOGGER.debug("Failed to move work area. Idling. Distance: " + getEntity().getPosition().distanceSq(_workArea));
                _currentTask = null;
                return State.IDLING;
            }
        }

        return State.TARGETING;
    }

    private void requestWorkAreas()
    {
        // TODO: Request work areas
        _workArea = null;
        _workAreas.clear();
        _requestEndMS = System.currentTimeMillis() + REQUEST_TIMEOUT_MS;
    }

    private void extractWorkAreas()
    {
        Iterator<Message> iter = _responses.iterator();
        while (iter.hasNext())
        {
            Message next =  iter.next();
            if (next instanceof MessageWorkArea)
            {
                if (_workArea == null && next.getCause() == _currentTask && System.currentTimeMillis() < _requestEndMS)
                    _workAreas.add(((MessageWorkArea) next).getWorkArea());

                iter.remove();
            }
        }
    }

    //=============================================================================================
    // PERFORMING

    private State perform()
    {
        // Update the task
        if (_currentTask != null)
        {
            _currentTask.updateTask();
            if (_currentTask.getState() == TaskBase.State.SUCCESS)
            {
                _currentTask = _currentTask.getNextTask();
                if (_currentTask != null)
                {
                    LOGGER.debug("Task complete, finding starting next: " + _currentTask);
                    tryMoveTo(_currentTask.getRequester().getPos());
                    return State.TRANSITING;
                }
                else
                {
                    LOGGER.debug("Task complete, switching to idle");
                    return State.IDLING;
                }
            }
            else if (_currentTask.getState() == TaskBase.State.FAILED)
            {
                LOGGER.debug("Task failed, switching to idle");
                return State.IDLING;
            }
        }

        return State.PERFORMING;
    }


    //=============================================================================================

    private State abortTaskChain()
    {
        _currentTask = null;
        return State.IDLING;
    }

    //=============================================================================================

    private boolean posInAreaXY(BlockPos pos, BlockPos center, int radius)
    {
        return (center.getX() - radius <= pos.getX() && pos.getX() <= center.getX() + radius &&
                center.getZ() - radius <= pos.getZ() && pos.getZ() <= center.getZ() + radius);
    }

    // Convenience method
    public boolean tryMoveTo(BlockPos pos)
    {
        LOGGER.debug("Speed: " + getEntity().getSpeed());
        return getEntity().getNavigator().tryMoveToXYZ(pos.getX(), pos.getY(), pos.getZ(), getEntity().getSpeed());
    }

    public boolean inProximity(BlockPos pos)
    {
        return DrudgeUtils.sqDistanceXY(getEntity().getPosition(), pos, 4);
    }

    //=============================================================================================

    class MyListener implements IListener
    {
        @Override
        public void handleMessage(Message message)
        {
            _messages.add(message);
        }
    }

    //========================
    // PRIVATES
    private EntityDrudge _entity;

    private enum State { IDLING, ELICITING, TRANSITING, TARGETING, PERFORMING }
    private State _state = State.IDLING;

    private static final int CHECK_DELAY_MILLIS = 3000;
    private static final int DEFAULT_RANGE = 10;
    private static final double DEFAULT_SPEED = 0.5;

    private long _nextElicit = 0;

    // Time we wait for messages.  5 ticks (250 ms) is usually good enough
    private static final long REQUEST_TIMEOUT_MS = 250;
    private long _requestEndMS = 0;

    // For Targeting
    private BlockPos _workArea = null;

    private TaskFactory _taskFactory = new TaskFactory();

    // Queue of messages
    private final Queue<Message> _messages = new LinkedTransferQueue<Message>();
    private final List<Message> _responses = new LinkedList<Message>();
    private final List<TaskBase> _proposedTasks = new ArrayList<TaskBase>();
    private final List<MessageTaskRequest> _responseTasks = new LinkedList<MessageTaskRequest>();
    private final List<BlockPos> _workAreas = new ArrayList<BlockPos>();

    // This is the channel we send and listen on.
    private Broadcaster.Channel _channel = Broadcaster.Channel.RED;

    // Do we have a current task we are pursuing?
    private TaskBase _currentTask;

    private static final Logger LOGGER = LogManager.getLogger();
}
