package com.crashbox.drudgemod.ai;

import com.crashbox.drudgemod.DrudgeUtils;
import com.crashbox.drudgemod.EntityDrudge;
import com.crashbox.drudgemod.messaging.*;
import com.crashbox.drudgemod.task.*;
import com.crashbox.drudgemod.task.TaskPair.Resolving;
import com.crashbox.drudgemod.task.TaskPair.UpdateResult;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.LinkedTransferQueue;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class EntityAIDrudge extends EntityAIBase implements IMessager
{
    public static TaskFactory TASK_FACTORY = new TaskFactory();

    public EntityAIDrudge(EntityDrudge entity)
    {
        this._entity = entity;
        Broadcaster.getInstance().subscribe(new MyListener());
        _entity.setCustomNameTag(makeName());
    }

    public EntityDrudge getEntity()
    {
        return _entity;
    }

    @Override
    public boolean shouldExecute()
    {
        updateTask();
        return (_state != State.IDLING);
    }

    @Override
    public void startExecuting()
    {
        // The update task loop starts it
//        _currentPair = null;
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
        reset();
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

            // Hand data requests.  These are generally simple status things -
            // TODO: Move this to the listener so it doesn't have to wait for the AI loop.
            if (msg instanceof MessageDataRequest)
            {
                processDataRequests((MessageDataRequest)msg);
                continue;
            }

            // Filter all task requests
            if (msg instanceof MessageTaskRequest && _currentPair == null)
            {
                if (msg.getTransactionID() == MessageWorkerAvailability.class)
                {
                    debugLog("Adding new task for message: " + msg);
                    _proposedTasks.add(makeNewTaskPair((MessageTaskRequest) msg));
                }
                else
                {
                    debugLog("Adding response task: " + msg);
                    _responseTasks.add((MessageTaskRequest) msg);
                }
            }
            else if (msg.getTransactionID() != null)
            {
                // If it has a transactionID it is a response to something we sent before, but isn't a task
                _responses.add(msg);
            }
            else
            {
                if (_currentPair != null && msg instanceof MessageTaskRequest )
                    debugLog("Have task, ignoring message: " + msg);
                else
                    debugLog("No task ignoring message: " + msg);
            }
        }
    }

    private void processDataRequests(MessageDataRequest msg)
    {
        if (msg instanceof MessageRequestWorkArea)
        {
            if (_workArea != null)
                Broadcaster.postMessage(new MessageWorkArea(this, msg.getSender(), msg.getTransactionID(), _workArea));
        }
    }

    private TaskPair makeNewTaskPair(MessageTaskRequest message)
    {
        TaskPair pair = new TaskPair(this);
        if (message instanceof MessageAcquireRequest)
        {
            TaskAcquireBase task = TASK_FACTORY.makeTaskFromMessage(this, (MessageAcquireRequest) message);
            pair.setAcquireTask(task);
        }
        else if (message instanceof MessageDeliverRequest)
        {
            TaskDeliverBase task = TASK_FACTORY.makeTaskFromMessage(this, (MessageDeliverRequest) message);
            pair.setDeliverTask(task);
        }
        else
        {
            LOGGER.error("makeTaskPair: Don't know what to do with task request: " + message);
        }

        return pair;
    }

    //=============================================================================================
    // ##### ####  #     ##### #   #  ####
    //   #   #   # #       #   ##  # #
    //   #   #   # #       #   # # # #  ##
    //   #   #   # #       #   #  ## #   #
    // ##### ####  ##### ##### #   #  ####

    private State idle()
    {
        // Once in a while we want to tell people we need more
        if (System.currentTimeMillis() > _nextElicit )
        {
            debugLog("Idle timeout over.");
            _nextElicit = System.currentTimeMillis() + ELICIT_DELAY_MS;
            _requestEndMS = System.currentTimeMillis() + REQUEST_TIMEOUT_MS;
            Broadcaster.postMessage(new MessageWorkerAvailability(_entity.worldObj, this));
            return State.ELICITING;
        }
        return State.IDLING;
    }


    //=============================================================================================
    // ##### #     #####  #### ##### ##### ##### #   #  ####
    // #     #       #   #       #     #     #   ##  # #
    // ####  #       #   #       #     #     #   # # # #  ##
    // #     #       #   #       #     #     #   #  ## #   #
    // ##### ##### #####  #### #####   #   ##### #   #  ####

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
            debugLog("Selecting from (" + _proposedTasks.size() + ") tasks.");
            _currentPair = Priority.selectBestTaskPair(getEntity().getPosition(), _proposedTasks, getEntity().getSpeed());
            _proposedTasks.clear();

            if (_currentPair != null)
            {
                _currentPair.start();
                sendAcceptedMessages(_currentPair);
            }
            else
            {
                return State.IDLING;
            }

            debugLog("Selected task: " + _currentPair);
            debugLog("   ==> moving to: " + _currentPair.getWorkCenter());
            tryMoveTo(_currentPair.getWorkCenter());
            return State.TRANSITING;
        }

        // Ask for a new task
        return State.ELICITING;
    }

    private void linkupResponses(List<MessageTaskRequest> responses)
    {
        for (TaskPair pair : _proposedTasks)
        {
            if (pair.getResolving() != Resolving.RESOLVING)
            {
                debugLog("linkupResponses: skipping pair, not resolving." + pair);
                continue;
            }

            if (linkupResponses(pair, responses))
            {
                // Since we linked something up, take it out of resolving.
                pair.setResolving(Resolving.UNRESOLVED);
            }
        }

        responses.clear();
    }

    private boolean linkupResponses(TaskPair pair, List<MessageTaskRequest> responses)
    {
        if (pair.getDeliverTask() == null)
            return linkupDeliverResponses(pair, responses);

        if (pair.getAcquireTask() == null)
            return linkupAcquireResponses(pair, responses);

        return false;
    }

    private boolean linkupDeliverResponses(TaskPair pair, List<MessageTaskRequest> responses)
    {

        BlockPos pos = getPos();
        boolean forEmpty = false;

        ItemStack held = getEntity().getHeldItem();
        if (pair.getDeliverTask() != null && pair.getEmptyInventory() == null && !
                pair.getDeliverTask().getMatcher().matches(held))
        {
            // This is for held.
            forEmpty = true;
        }
        else
        {
            if (pair.getAcquireTask() != null)
                pos = pair.getAcquireTask().getCoarsePos();
        }

        List<MessageDeliverRequest> delivers = extractMessages(responses, MessageDeliverRequest.class);
        if (delivers.size() > 0)
        {
            MessageDeliverRequest best = findBest(pos, delivers);
            if (DrudgeUtils.isNotNull(best, LOGGER))
            {
                if (forEmpty)
                    pair.setEmptyInventory(TASK_FACTORY.makeTaskFromMessage(this, best));
                else
                    pair.setDeliverTask(TASK_FACTORY.makeTaskFromMessage(this, best));

                return true;
            }
        }

        return false;
    }

    private boolean linkupAcquireResponses(TaskPair pair, List<MessageTaskRequest> responses)
    {
        debugLog("Linking up acquire responses.");
        BlockPos pos = getPos();
        if (pair.getEmptyInventory() != null)
            pos = pair.getEmptyInventory().getCoarsePos();

        List<MessageAcquireRequest> acquires = extractMessages(responses, MessageAcquireRequest.class);
        debugLog("Have (" + acquires.size() + ") acquires ");
        if (acquires.size() > 0)
        {
            MessageAcquireRequest best = findBest(pos, acquires);
            debugLog("Best acquire: " + best);
            if (DrudgeUtils.isNotNull(best, LOGGER))
            {
                pair.setAcquireTask(TASK_FACTORY.makeTaskFromMessage(this, best));
                return true;
            }
        }
        return false;
    }

    private <T extends MessageTaskRequest> T findBest(BlockPos pos, List<T> responses)
    {
        T best = null;
        int bestValue = Integer.MIN_VALUE;

        for (T msg : responses)
        {
            int value = Priority.computeDistanceCost(pos, msg.getSender().getPos()) + msg.getValue();
            if (value > bestValue)
            {
                bestValue = value;
                best = msg;
            }
        }
        return best;
    }

    @SuppressWarnings("unchecked")
    private <T extends MessageTaskRequest> List<T> extractMessages(List<MessageTaskRequest> responses, Class<T> clazz)
    {
        List<T> result = new ArrayList<T>();
        Iterator<MessageTaskRequest> iter = responses.iterator();
        while (iter.hasNext())
        {
            MessageTaskRequest next =  iter.next();
            if (clazz.isInstance(next))
            {
                result.add((T) next);
                iter.remove();
            }
        }
        return result;
    }

    //==================

    private void resolveAllTasks()
    {
        for (TaskPair pair : _proposedTasks)
        {
            if (pair.getResolving() == Resolving.UNRESOLVED)
            {
                // Get a new message send it out
                Message msg = resolveTaskPair(pair);
                debugLog("Resolving : " + pair);
                if (msg != null)
                {
                    debugLog("   == With message: " + msg);
                    pair.setResolving(Resolving.RESOLVING);
                    Broadcaster.postMessage(msg);
                }
                else
                {
                    pair.setResolving(Resolving.RESOLVED);
                }
            }
        }
    }

    private Message resolveTaskPair(TaskPair pair)
    {
        // If we have a deliver we need to make sure we have the item
        ItemStack held = getEntity().getHeldItem();

        // First off let's see if we need to dump the thing in our hand.  We figure this out
        // first because to compute best 'acquire' cost depends on where we are at the end
        // of the empty inventory step.

        if (held != null && pair.getDeliverTask() != null && !pair.getDeliverTask().getMatcher().matches(held))
        {
            // We need to dump something we are holding before we can acquire
            return new MessageStorageRequest(this, null, pair, 0, held);
        }

        // Need to deliver, do we need to acquire?
        if (pair.getDeliverTask() != null && pair.getAcquireTask() == null)
        {
            if (held == null || !pair.getDeliverTask().getMatcher().matches(held))
            {
                return new MessageItemRequest(this, null, pair, pair.getDeliverTask().getMatcher(),
                        pair.getDeliverTask().getQuantity());
            }

            // If we are here, held item matches...
        }

        // We accepted an acquire before deliver.  So a really full chest or orchard
        if (pair.getAcquireTask() != null && pair.getDeliverTask() == null)
        {
            return new MessageStorageRequest(this, null, pair, 0, pair.getAcquireTask().getSample());
        }

        // Everybody is good, we don't need anything
        return null;
    }

    private void sendAcceptedMessages(TaskPair pair)
    {
        int delay = 0;
        BlockPos pos = getEntity().getPosition();

        for (TaskBase task : pair.asList())
        {
            if (task != null)
            {
                // Five hundred millis for each block we need to walk. TODO:  Rework in entity speed.
                delay += Priority.computeDistanceCost(pos, task.getCoarsePos()) * 500;
                pos = task.getCoarsePos();
                // Add two seconds to break, pickup, place, etc.
                delay += 2000;
                Broadcaster.postMessage(new MessageWorkAccepted(this, task.getRequester(), null, 0, delay));
            }
        }
    }

    //=============================================================================================
    // ##### ####   ###  #   #  ###  ##### ##### #####  ###  #   # ##### #   #  ####
    //   #   #   # #   # ##  # #       #     #     #   #   # ##  #   #   ##  # #
    //   #   ####  ##### # # #  ###    #     #     #   #   # # # #   #   # # # #  ##
    //   #   #   # #   # #  ##     #   #     #     #   #   # #  ##   #   #  ## #   #
    //   #   #   # #   # #   #  ###  #####   #   #####  ###  #   # ##### #   #  ####

    // In this function we transition to the target site. It might be far away.
    private State transition()
    {
        // If within 20, then issue location request and to start targeting
        if (posInAreaXY(getPos(), _currentPair.getWorkCenter(), 20))
        {
            LOGGER.debug(id() + " Within distance, switching to targeting.");
            requestWorkAreas();
            return State.TARGETING;
        }
        else if (getEntity().getNavigator().noPath())
        {
            LOGGER.debug(id() + " Couldn't get a path during transition, idling");
            // If we have no path, then we are done.
            resetTask();
            return State.IDLING;
        }

        return State.TRANSITING;
    }

    //=============================================================================================
    // #####  ###  ####   #### ##### ##### ##### #   #  ####
    //   #   #   # #   # #     #       #     #   ##  # #
    //   #   ##### ####  #  ## ####    #     #   # # # #  ##
    //   #   #   # #   # #   # #       #     #   #  ## #   #
    //   #   #   # #   #  #### #####   #   ##### #   #  ####

    private State target()
    {
        // Collect work area messages from other bots
        extractWorkAreas();

        // After some period of time, get the task top generate a workArea based on its specifics
        if (_workArea == null && System.currentTimeMillis() > _requestEndMS)
        {
            _workArea = _currentPair.getWorkTarget(_workAreas);
            if (_workArea == null)
            {
                LOGGER.debug(id() + " Failed to find work area, aborting. " + _currentPair);
                _currentPair = null;
                return State.IDLING;
            }

            LOGGER.debug(id() + " Determining work area and redirecting: " + _workArea + " currently at: " + getPos());
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
                LOGGER.debug("Failed to move to work area. Idling. Distance: " +
                        DrudgeUtils.sqDistXZ(getEntity().getPosition(), _workArea));
                _currentPair = null;
                return State.IDLING;
            }
        }

        return State.TARGETING;
    }

    private void requestWorkAreas()
    {
        Broadcaster.postMessage(new MessageRequestWorkArea(this, null, _currentPair, 0));

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
                if (_workArea == null && next.getTransactionID() == _currentPair && System.currentTimeMillis() < _requestEndMS)
                    _workAreas.add(((MessageWorkArea) next).getWorkArea());

                iter.remove();
            }
        }
    }

    /**
     * Used by tasks to update the work area once we are working.
     * @param pos The new work area.
     */
    public void updateWorkArea(BlockPos pos)
    {
        _workArea = pos;
    }

    //=============================================================================================
    // ####  ##### ####  #####  ###  ####  #   # ##### #   #  ####
    // #   # #     #   # #     #   # #   # ## ##   #   ##  # #
    // ####  ####  ####  ####  #   # ####  # # #   #   # # # #  ##
    // #     #     #   # #     #   # #   # #   #   #   #  ## #   #
    // #     ##### #   # #      ###  #   # #   # ##### #   #  ####

    private State perform()
    {
        // Keep doing the task until we run out.
        if (_currentPair != null)
        {
            switch (_currentPair.updateTask())
            {
                case CONTINUE:
                    // Nothing special.
                    return State.PERFORMING;
                case RETARGET:
                    debugLog(" Retargeting");
                    requestWorkAreas();
                    return State.TARGETING;
                case DONE:
                    debugLog(" Switching to idle.");
                    return State.IDLING;
            }
        }

        return State.IDLING;
    }


    private void reset()
    {
        _state = State.IDLING;
        _currentPair = null;
        _workArea = null;
        _proposedTasks.clear();
        _responses.clear();
        _nextElicit = System.currentTimeMillis() + ELICIT_DELAY_MS;
    }

    //=============================================================================================
    //=============================================================================================
    // #   # ##### ##### #      ###
    // #   #   #     #   #     #
    // #   #   #     #   #      ###
    // #   #   #     #   #         #
    //  ###    #   ##### #####  ###

    private boolean posInAreaXY(BlockPos pos, BlockPos center, int radius)
    {
        return (center.getX() - radius <= pos.getX() && pos.getX() <= center.getX() + radius &&
                center.getZ() - radius <= pos.getZ() && pos.getZ() <= center.getZ() + radius);
    }

    // Convenience method
    public boolean tryMoveTo(BlockPos pos)
    {
        return getEntity().getNavigator().tryMoveToXYZ(pos.getX(), pos.getY(), pos.getZ(), getEntity().getSpeed());
    }

    public boolean inProximity(BlockPos pos)
    {
        return DrudgeUtils.isWithinSqDist(getEntity().getPosition(), pos, 9);
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


    //=============================================================================================

    public String id()
    {
        return getEntity().getCustomNameTag() + ":" + _state.name();
    }

    public void errorLog(String message)
    {
        LOGGER.error(id() + " " + message);
    }

    public void infoLog(String message)
    {
        LOGGER.info(id() + " " + message);
    }

    public void debugLog(String message)
    {
        LOGGER.debug(id() + " " + message);
    }


    @Override
    public String toString()
    {
        return id() +
                "{ _state=" + _state +
                ", _currentPair=" + _currentPair +
                ", _nextElicit=" + _nextElicit +
                ", _requestEndMS=" + _requestEndMS +
                ", _workArea=" + _workArea +
                ", _messagesSize=" + _messages.size() +
                ", _responsesSize=" + _responses.size() +
                ", _proposedTasksSize=" + _proposedTasks.size() +
                ", _responseTasksSize=" + _responseTasks.size() +
                '}';
    }


    //=============================================================================================
    // NAMING

    private static String[] NAMES = { "takara", "akai", "frodo", "sam", "merry", "pippin", "gimli", "legolas", "larry", "moe", "curly", "sleepy", "grumpy", "dopey", "doc", "bashful" };

    private static int NAME_INDEX = 0;

    private static String makeName()
    {
        int idx = NAME_INDEX % NAMES.length;
        int suffix = NAME_INDEX / NAMES.length;
        String name = NAMES[idx];
        if (suffix > 0)
            name = name + suffix;

        NAME_INDEX++;

        return name;
    }


    //=============================================================================================
    //=============================================================================================

    // PRIVATES
    private EntityDrudge _entity;

    private enum State {
        IDLING,         // Don't currently have work.  Usually not moving.  We can do other AI ops.
        ELICITING,      // Messaging, looking for work
        TRANSITING,     // Coarse grained movement
        TARGETING,      // Fine grained movement
        PERFORMING      // Within distance of pos.
    }

    // Main state variable for the loop
    private State _state = State.IDLING;


    // We don't want to ask for work too often.  If we don't get a response, just hang out.
    private static final int ELICIT_DELAY_MS = 6000;
    private long _nextElicit = 0;


    private static final int DEFAULT_RANGE = 10;
    private static final double DEFAULT_SPEED = 0.5;


    // Time we wait for messages.  5 ticks (250 ms) is usually good enough
    private static final long REQUEST_TIMEOUT_MS = 250;
    private long _requestEndMS = 0;

    // For Targeting
    private BlockPos _workArea = null;


    private final List<TaskPair> _proposedTasks = new ArrayList<TaskPair>();

    // Queue of messages
    private final Queue<Message> _messages = new LinkedTransferQueue<Message>();
    private final List<Message> _responses = new LinkedList<Message>();
    private final List<MessageTaskRequest> _responseTasks = new LinkedList<MessageTaskRequest>();

    private final List<BlockPos> _workAreas = new ArrayList<BlockPos>();

    // Do we have a current task we are pursuing?
    private TaskPair _currentPair;

    // We have two basic actions.  Sometimes we need to empty our contents first
    //private TaskBase _emptyContents;
    private TaskAcquireBase _acquireTask;
    private TaskDeliverBase _deliverTask;

    private static final Logger LOGGER = LogManager.getLogger();
}
