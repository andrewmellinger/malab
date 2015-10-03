package com.crashbox.vassal.ai;

import com.crashbox.vassal.VassalUtils;
import com.crashbox.vassal.entity.EntityVassal;
import com.crashbox.vassal.common.ItemStackMatcher;
import com.crashbox.vassal.entity.RenderVassal;
import com.crashbox.vassal.messaging.*;
import com.crashbox.vassal.task.*;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.LinkedTransferQueue;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class EntityAIVassal extends EntityAIBase implements IMessager
{
    public EntityAIVassal(EntityVassal entity)
    {
        this._entity = entity;
        Broadcaster.getInstance().subscribe(new MyListener());
        _entity.setCustomNameTag(makeName());
        _nextElicit = System.currentTimeMillis() + ELICIT_DELAY_MS +
                (long)(ELICIT_DELAY_MS * _entity.getRNG().nextFloat());
    }

    public EntityVassal getEntity()
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
        // Nothing to do as we were idling in shouldExecute
    }

    @Override
    public boolean continueExecuting()
    {
        return _state != State.IDLING;
    }

    @Override
    public void resetTask()
    {
        // NOTE:  Reset is called every time we complete.
        cancel();
    }

    @Override
    public void updateTask()
    {
        // Process messages should handle data requests
        processMessages();

        ensureFuel();

        // If we can't run (no fuel, etc.) then just return.
        // We still answered messages from above.
        if (!canRun())
            return;

        if (_paused)
        {
            if ((System.currentTimeMillis() / 1000) > _pausedMessageSecs)
            {
                LOGGER.debug("----- PAUSED -----");
                _pausedMessageSecs = (System.currentTimeMillis() / 1000);
            }
            return;
        }

        // As long as we have a task, tell them we are working on it
        handleHeartbeat();
        handleHealing();

        //LOGGER.debug("UpdateTask: " + _state);
        switch (_state)
        {
            case IDLING:
                getEntity().setCurrentItemOrArmor(3, null);
                _state = idle();
                break;
            case ELICITING:
                _state = elicit();
                break;
            case TRANSITING:
                _state = transition();
                break;
            case TARGETING:
                burnFuel();
                _state = target();
                break;
            case PERFORMING:
                burnFuel();
                _state = perform();
                break;
        }
    }

    private long _pausedMessageSecs = 0;

    // ================
    // IMessager

    @Override
    public BlockPos getBlockPos()
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
            ItemStack held = getEntity().getHeldItem();
            if (msg instanceof MessageTaskRequest && _currentTask == null)
            {
                if (msg.getTransactionID() == MessageWorkerAvailability.class)
                {
                    debugLog("Adding new task for message: " + msg);
                    _proposedTasks.add(makeNewTask((MessageTaskRequest) msg));
                }
//                else if (msg instanceof TRStore && held != null && msg.getTransactionID() == held.getItem())
                else if (msg instanceof TRDeliverBase && held != null && msg.getTransactionID() == held.getItem())
                {
                    debugLog("Adding new Deliver task : " + msg);
                    _proposedTasks.add(makeNewTask((MessageTaskRequest) msg));
                }
                else
                {
                    debugLog("Adding response task: " + msg);
                    _responseTasks.add((MessageTaskRequest) msg);
                }
            }
            else if (msg instanceof MessageTaskPairRequest)
            {
                if (msg.getTransactionID() == MessageWorkerAvailability.class)
                {
                    debugLog("Adding new tasks for PAIR message: " + msg);
                    _proposedTasks.add(makeNewTask((MessageTaskPairRequest) msg));
                }
            }
            else if (msg.getTransactionID() != null)
            {
                // If it has a transactionID it is a response to something we sent before, but isn't a task
                _responses.add(msg);
            }
            else
            {
                if (_currentTask != null && msg instanceof MessageTaskRequest )
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

    private ITask makeNewTask(MessageTaskRequest message)
    {
        TaskPair pair = new TaskPair(this);
        if (message instanceof TRAcquireBase)
        {
            TaskAcquireBase task = (TaskAcquireBase)TaskBase.createTask(this, message);
            pair.setAcquireTask(task);
        }
        else if (message instanceof TRDeliverBase)
        {
            TaskDeliverBase task = (TaskDeliverBase)TaskBase.createTask(this, message);
            pair.setDeliverTask(task);
        }
        else
        {
            LOGGER.error("makeTaskPair: Don't know what to do with task request: " + message);
        }

        return pair;
    }

    private ITask makeNewTask(MessageTaskPairRequest message)
    {
        TaskPair pair = new TaskPair(this);

        TaskAcquireBase taskAcquire = (TaskAcquireBase)TaskBase.createTask(this, message.getAcquireRequest());
        pair.setAcquireTask(taskAcquire);

        TaskDeliverBase taskDeliver= (TaskDeliverBase)TaskBase.createTask(this, message.getDeliverRequest());
        pair.setDeliverTask(taskDeliver);

        // Should we repeat?
        pair.setRepeat(message.getRepeat());

        return pair;
    }

    private void handleHeartbeat()
    {
        long now = System.currentTimeMillis();
        if (_currentTask != null && now > _nextHeartbeat)
        {
            _nextHeartbeat = now + HEARTBEAT_DELAY;
            _currentTask.sendHeartbeat(_nextHeartbeat + HEARTBEAT_VARIANCE);
        }
    }

    private void handleHealing()
    {
//        debugLog("aiMoveSpeed=" + _entity.getAIMoveSpeed() + ", health=" + _entity.getHealth() +
//                ", max=" + _entity.getMaxHealth() + ", _fuelTicks=" + _fuelTicks);
//        public float getAIMoveSpeed()
//        public void setAIMoveSpeed(float p_70659_1_)

        if (_entity.getHealth() < _entity.getMaxHealth())
        {
            if (System.currentTimeMillis() > _nextHeal && _fuelTicks > HEAL_FUEL_PER_HALF_HEART)
            {
                _entity.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 20, 1));
                _fuelTicks -= HEAL_FUEL_PER_HALF_HEART;
                _nextHeal = System.currentTimeMillis() + HEAL_DELAY;
                _entity.heal(1.0F);
            }
        }
    }

    //=============================================================================================
    // ##### ####  #     ##### #   #  ####
    //   #   #   # #       #   ##  # #
    //   #   #   # #       #   # # # #  ##
    //   #   #   # #       #   #  ## #   #
    // ##### ####  ##### ##### #   #  ####

    private State idle()
    {
        // Why separate times?  Well, we don't want to be useless while being low on fuel
        // Why? Well, we may be able to collect trees and make fuel. So we can influence
        // our own fuel production.
        // Once in a while we want to tell people we need more
        if (System.currentTimeMillis() > _nextFuel && needFuel() )
        {
            debugLog("Low on fuel, requesting!!!");
            _nextFuel = System.currentTimeMillis() + FUEL_REQUEST_DELAY;
            _requestEndMS = System.currentTimeMillis() + REQUEST_TIMEOUT_MS;

            postFuelRequest();
            return State.ELICITING;
        }
        else if (System.currentTimeMillis() > _nextElicit )
        {
            // Once in a while we want to tell people we need more
            //debugLog("Idle timeout over.");
            _nextElicit = System.currentTimeMillis() + ELICIT_DELAY_MS;
            _requestEndMS = System.currentTimeMillis() + REQUEST_TIMEOUT_MS;

            // TODO:  What if I am holding fuel?
            ItemStack held = getEntity().getHeldItem();
            if (held != null)
                Broadcaster.postMessage(new MessageIsStorageAvailable(this, null, held.getItem(), 0, new ItemStackMatcher(held)));
            else
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
        linkupResponses();

        // Try to resolve to the issue back messages
        resolveAllTasks();

        // IMPROVEMENT:  We could stop accepting new ones and only process resolves.  This
        // could shorten the time we wait.  So have two timeouts.  New starter task arrival
        // and entire elicitation arrival.

        // When we hit the timeout we are done.
        if (System.currentTimeMillis() > _requestEndMS)
        {
            debugLog("Selecting from (" + _proposedTasks.size() + ") tasks.");
            _currentTask = Priority.selectBestTask(getEntity().getPosition(), _proposedTasks, getEntity().getSpeed());
            _proposedTasks.clear();

            if (_currentTask != null)
                _currentTask.start();
            else
                return State.IDLING;

            //getEntity().spawnExplosionParticle();
            BlockPos workCenter = _currentTask.getWorkCenter();
            debugLog("Selected task: " + _currentTask);
            debugLog("   ==> moving to: " + workCenter);
            tryMoveTo(workCenter);

            // Look at the work center
//            _entity.getLookHelper().setLookPosition(workCenter.getX(), workCenter.getY(), workCenter.getZ(),
//                    0, _entity.getVerticalFaceSpeed());
            return State.TRANSITING;
        }

        // Ask for a new task
        return State.ELICITING;
    }

    private void linkupResponses()
    {
        for (ITask task : _proposedTasks)
        {
            // Extract any messages for this task
            List<MessageTaskRequest> responses = extractResponsesToID(task);
            task.linkupResponses(responses);
        }

        _responseTasks.clear();
    }

    private List<MessageTaskRequest> extractResponsesToID(Object id)
    {
        List<MessageTaskRequest> result = new ArrayList<MessageTaskRequest>();
        Iterator<MessageTaskRequest> iter = _responseTasks.iterator();
        while (iter.hasNext())
        {
            MessageTaskRequest next =  iter.next();
            if (next.getTransactionID() == id)
            {
                result.add(next);
                iter.remove();
            }
        }
        return result;
    }

    //==================

    private void resolveAllTasks()
    {
        for (ITask task : _proposedTasks)
        {
            task.resolve();
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
        // If within our distance, then issue location request and to start targeting
        if (posInAreaXY(getBlockPos(), _currentTask.getWorkCenter(), TARGETING_DISTANCE))
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
            _workArea = _currentTask.getWorkTarget(_workAreas);
            if (_workArea == null)
            {
                LOGGER.debug(id() + " Failed to find work area, aborting. " + _currentTask);
                _currentTask = null;
                return State.IDLING;
            }

            LOGGER.debug(id() + " Determining work area and redirecting: " + _workArea + " currently at: " +
                    getBlockPos());
            _workAreaAttempt = 0;
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
                if (_workAreaAttempt == 3)
                {
                    LOGGER.debug("Failed to move to work area. IDLING. Distance: " +
                            VassalUtils.sqDistXZ(getEntity().getPosition(), _workArea));
                    _currentTask = null;
                    return State.IDLING;
                }
                else
                {
                    LOGGER.debug("Failed to move to work area. TRYING AGAIN.");
                    _workAreaAttempt++;
                    tryMoveTo(_workArea);
                }
            }
        }

        return State.TARGETING;
    }

    private void requestWorkAreas()
    {
        Broadcaster.postMessage(new MessageRequestWorkArea(this, null, _currentTask, 0));

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
                if (_workArea == null && next.getTransactionID() == _currentTask && System.currentTimeMillis() < _requestEndMS)
                    _workAreas.add(((MessageWorkArea) next).getWorkArea());

                iter.remove();
            }
        }
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
        if (_currentTask != null)
        {
            switch (_currentTask.updateTask())
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
                    debugLog(this.toString());
                    debugLog("Now: " + System.currentTimeMillis());
                    return State.IDLING;
            }
        }

        return State.IDLING;
    }

    private void cancel()
    {
        _state = State.IDLING;
        _currentTask = null;
        _workArea = null;
        _proposedTasks.clear();
        _responses.clear();
        getEntity().getNavigator().clearPathEntity();

        long tmp = System.currentTimeMillis() + ELICIT_RESET_DELAY;
        if (tmp > _nextElicit)
            _nextElicit = tmp;
    }

    //=============================================================================================
    //=============================================================================================

    public void cancelAndPause()
    {
        debugLog("---------------------->>>>>>> PAUSING <<<<<<<<<<<<<<<<<<<----------------");
        _paused = true;
        cancel();
    }

    public void resume()
    {
        _nextElicit = System.currentTimeMillis() + ELICIT_DELAY_MS;
        _paused = false;
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
        // If we are 2 blocks away, we are good enough

        //if (inProximity(pos))
        if (VassalUtils.isWithinSqDist(getEntity().getPosition(), pos, 4))
        {
            debugLog("Close enough!  Not moving.");
            return true;
        }

        // Computa position towards us but not on the block, so we aren't actually standing on the thing.
        BlockPos target = VassalUtils.getBlockBeside(getBlockPos(), pos);
        debugLog("Targeting  Nearby: " + pos + " to: " + target);
        //target = pos;
        return getEntity().getNavigator().tryMoveToXYZ(target.getX(), target.getY(), target.getZ(),
                getEntity().getSpeed());
    }

    public boolean inProximity(BlockPos pos)
    {
        return VassalUtils.isWithinSqDist(getEntity().getPosition(), pos, PROXIMITY_SQ);
    }

    //=============================================================================================

    public int getFuelTicks()
    {
        return _fuelTicks;
    }

    public void setFuelTicks(int ticks)
    {
        _fuelTicks = ticks;
    }

    private boolean canRun()
    {
        // If we have fuel ticks or spare fuel then we can run
        return (_fuelTicks > 0);
    }


    private void ensureFuel()
    {
        if (_fuelTicks == 0)
        {
            ItemStack fuelStack = getEntity().getFuelStack();
            if (fuelStack != null)
            {
                _fuelTicks = TileEntityFurnace.getItemBurnTime(fuelStack);
                fuelStack.stackSize--;
                if (fuelStack.stackSize == 0)
                    _entity.setFuelStack(null);
                else
                    _entity.setFuelStack(fuelStack);
            }
        }
    }

    private boolean burnFuel()
    {
        ensureFuel();
        if (_fuelTicks != 0)
        {
            --_fuelTicks;
            return true;
        }

        // If we got here out of fuel and nothing in stack
        cancel();
        return false;
    }

    private boolean needFuel()
    {
        ItemStack stack = _entity.getFuelStack();
        return (stack == null || stack.stackSize < 4);
    }

    private void postFuelRequest()
    {
        // Add the task to put the item in our inventory then post special
        TaskPair pair = new TaskPair(this);

        ItemStack stack = _entity.getFuelStack();
        ItemStackMatcher matcher;
        if (stack == null)
            matcher = new ItemStackMatcher(new ItemStack(Items.coal, 0, 1));
        else
            matcher = new ItemStackMatcher(stack);

        // The system will then ask for some.
        pair.setDeliverTask(new TaskRefuel(this, this, matcher, 8));
        _proposedTasks.add(pair);
    }

    //=============================================================================================

    public static void setRenderVassal(RenderVassal render)
    {
        _renderVassal = render;
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
                ", _currentTask=" + _currentTask +
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
    private EntityVassal _entity;

    private enum State {
        IDLING,         // Don't currently have work.  Usually not moving.  We can do other AI ops.
        ELICITING,      // Messaging, looking for work
        TRANSITING,     // Coarse grained movement
        TARGETING,      // Fine grained movement
        PERFORMING      // Within distance of pos.
    }

    // How many ticks of fuel do I have left.
    private int _fuelTicks;

    // Main state variable for the loop
    private State _state = State.IDLING;
    private boolean _paused = false;

    // We don't want to ask for work too often.  If we don't get a response, just hang out.
    private static final int ELICIT_DELAY_MS = 6000;
    private static final int ELICIT_RESET_DELAY = 500;
    private long _nextElicit = 0;

    private static final int FUEL_REQUEST_DELAY = 2000;
    private long _nextFuel = 0;

    // How often do we send the hearbeat, and how long should they wait
    private static final long HEARTBEAT_DELAY       = 2000;
    private static final long HEARTBEAT_VARIANCE    = 1000;
    private long _nextHeartbeat;

    private static final long HEAL_DELAY                = 500;
    private static final long HEAL_FUEL_PER_HALF_HEART  = 20;
    private long _nextHeal                              = 0;

//    private static final int DEFAULT_RANGE = 10;
//    private static final double DEFAULT_SPEED = 0.5;
    public static final int TARGETING_DISTANCE = 16;

    // Time we wait for messages.  5 ticks (250 ms) is usually good enough
    private static final long REQUEST_TIMEOUT_MS = 250;
    private long _requestEndMS = 0;

    // For Targeting
    private BlockPos _workArea = null;
    private int _workAreaAttempt;

    private final List<ITask> _proposedTasks = new ArrayList<ITask>();

    // Queue of messages
    private final Queue<Message> _messages = new LinkedTransferQueue<Message>();
    private final List<Message> _responses = new LinkedList<Message>();
    private final List<MessageTaskRequest> _responseTasks = new LinkedList<MessageTaskRequest>();

    private final List<BlockPos> _workAreas = new ArrayList<BlockPos>();

    // Do we have a current task we are pursuing?
    private ITask _currentTask;

    // Visual things
    private static RenderVassal _renderVassal;

    private static final int PROXIMITY_SQ = 11;
    private static final Logger LOGGER = LogManager.getLogger();
}
