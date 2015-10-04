package com.crashbox.vassal.task;

import com.crashbox.vassal.VassalUtils;
import com.crashbox.vassal.ai.EntityAIVassal;
import com.crashbox.vassal.ai.Priority;
import com.crashbox.vassal.messaging.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Copyright 2015 Andrew O. Mellinger
 *
 * This is a simple object to track the tasks that make up a set.
 */
public class TaskPair implements ITask
{
    public enum Stage { EMPTYING, ACQUIRING, DELIVERING, DONE}

    public TaskPair(EntityAIVassal entityAI)
    {
        _entityAI = entityAI;
    }

    public TaskAcquireBase getAcquireTask()
    {
        return _acquireTask;
    }

    public void setAcquireTask(TaskAcquireBase acquireTask)
    {
        _acquireTask = acquireTask;
    }

    public TaskDeliverBase getDeliverTask()
    {
        return _deliverTask;
    }

    public void setDeliverTask(TaskDeliverBase deliverTask)
    {
        _deliverTask = deliverTask;
    }

    //====
    public Resolving getResolving()
    {
        return _resolving;
    }

    public void setResolving(Resolving resolving)
    {
        _resolving = resolving;
    }

    public void setRepeat(boolean repeat)
    {
        _repeat = repeat;
    }


    public boolean resolve()
    {
        if (getResolving() == Resolving.RESOLVED)
            return true;

        if (getResolving() == Resolving.RESOLVING)
            return false;

        // If we have a deliver we need to make sure we have the item
        ItemStack held = _entityAI.getEntity().getHeldItem();

        // Need to deliver, do we need to acquire?
        if (getDeliverTask() != null && getAcquireTask() == null)
        {
            // If we are holding the right thing, just deliver it
            if (held == null || !getDeliverTask().getMatcher().matches(held))
            {
                Broadcaster.postMessage(new MessageItemRequest(_entityAI, null, this, getDeliverTask().getMatcher(),
                        getDeliverTask().getQuantity()));
                setResolving(Resolving.RESOLVING);
                return false;
            }
        }

        // We accepted an acquire before deliver.  So a really full chest or orchard
        if (getAcquireTask() != null && getDeliverTask() == null)
        {
            Broadcaster.postMessage(new MessageIsStorageAvailable(_entityAI, null, this, 0, getAcquireTask().getMatcher()));
            setResolving(Resolving.RESOLVING);
            return false;
        }

        // Everybody is good, we don't need anything
        setResolving(Resolving.RESOLVED);
        return true;
    }

    @Override
    public void linkupResponses(List<MessageTaskRequest> responses)
    {
        if (getDeliverTask() == null)
            linkupDeliverResponses(responses);

        if (getAcquireTask() == null)
            linkupAcquireResponses(responses);
    }

    public int getValue(double speed)
    {
        TaskBase[] tasks = { _acquireTask, _deliverTask};
        BlockPos pos = _entityAI.getBlockPos();
        int value = 0;
        for (TaskBase task : tasks)
        {
            if (task != null)
            {
                int cost = Priority.computeDistanceCost(pos, task.getWorkCenter(), speed);
                int val = task.getValue();
                value = value - cost + val;
                pos = task.getWorkCenter();
            }
        }
        return value;
    }

    public void sendHeartbeat(long expire)
    {
        // Send one to acquire if we have one and we are working on it
        if (_current == _acquireTask && _acquireTask != null)
            sendHeartbeatFor(_acquireTask, expire);

        // We are always working before or on deliver
        if (_deliverTask != null )
            sendHeartbeatFor(_deliverTask, expire);
    }

    private void sendHeartbeatFor(TaskBase task, long expire)
    {
        Broadcaster.postMessage(new MessageWorkingHeartbeat(_entityAI, task.getRequester(), task, expire));
    }

    public void start()
    {
        if (_acquireTask != null)
        {
            _current = _acquireTask;
            _stage = Stage.ACQUIRING;
        }
        else if (_deliverTask != null)
        {
            _current = _deliverTask;
            _stage = Stage.DELIVERING;
        }
        else
        {
            LOGGER.error("Starting task pair but didn't have any tasks!");
            _stage = Stage.DONE;
        }
    }

    /**
     * This gets the general work area, usually the location of a beacon.
     * @return The general area of work.
     */
    public BlockPos getWorkCenter()
    {
        return _current.getWorkCenter();
    }

    /**
     * Returns the next work available work target.  Note, this will/may change
     * each time it is called.  If it returns null, then no more work locations
     * are available.
     * @param exclusions Work areas to skip.
     * @return A position of a block in which to work on or null for none.
     */
    public BlockPos getWorkTarget(List<BlockPos> exclusions)
    {
        BlockPos workArea = _current.getWorkTarget(exclusions);

        if (workArea != null)
            return workArea;

        // If we have no work area but we have something to deliver, let's just do that.
        if (_stage == Stage.ACQUIRING && _entityAI.getEntity().getHeldSize() > 0)
        {
            _stage = Stage.DELIVERING;
            _current = _deliverTask;
        }
        else
        {
            // We have nothing and we can't acquire.  Give up.
            return null;
        }

        return _current.getWorkTarget(exclusions);
    }

    /**
     * Calls the current task to progress.  Returns a value describing the state
     * of the pair.
     * @return Enum describing what else to do.
     */
    public UpdateResult updateTask()
    {
        // When done we need to move to the next thing, or we are completely done
        UpdateResult result = _current.executeAndIsDone();
        if (result == UpdateResult.CONTINUE ||
                result == UpdateResult.RETARGET )
        {
            return result;
        }

        // If we are here it is done.
        switch (_stage)
        {
            case EMPTYING:
                _current = _acquireTask;
                _stage = Stage.ACQUIRING;
                LOGGER.debug(_entityAI.id() + ":  finished acquiring.");
                return UpdateResult.RETARGET;

            case ACQUIRING:
                // If we have enough stuff, then we are good
                if (acquiredEnough())
                {
                    LOGGER.debug(_entityAI.id() + ":  acquiredEnough.");
                    _current = _deliverTask;
                    _stage = Stage.DELIVERING;
                }
                return UpdateResult.RETARGET;

            case DELIVERING:
                LOGGER.debug(_entityAI.id() + ":  finished delivering.");
                if (_repeat)
                {
                    // If we are supposed to repeat then keep delivering until we run out
                    // or switch to reaquire until it runs out.
                    if (_entityAI.getEntity().getHeldSize() == 0)
                    {
                        _stage = Stage.ACQUIRING;
                        _current = _acquireTask;
                    }

                    return UpdateResult.RETARGET;
                }

                // Since we have no repeat, we are done.
                _current = null;
                _stage = Stage.DONE;

                return UpdateResult.DONE;
        }

        return UpdateResult.CONTINUE;
    }

    /**
     * @return True if entity is done collecting.
     */
    private boolean acquiredEnough()
    {
        // -1 is sentinel value forget as much as you can
        if (_deliverTask.getQuantity() == -1)
        {
            return _entityAI.getEntity().isHeldInventoryFull();
        }

        return _entityAI.getEntity().isHeldInventoryFull() ||
               _entityAI.getEntity().getHeldSize() >= _deliverTask.getQuantity();
    }

    private boolean linkupDeliverResponses(List<MessageTaskRequest> responses)
    {
        // If not an empty, we don't need two deliver tasks.
        if (getDeliverTask() != null)
            return false;

        List<TRDeliverBase> delivers = MessageUtils.extractMessages(this, responses, TRDeliverBase.class);
        if (delivers.size() == 0)
            return false;

        BlockPos pos = _entityAI.getBlockPos();
        if (getAcquireTask() != null)
            pos = getAcquireTask().getWorkCenter();

        // Now, find the best one based on what we are going to do with it
        TRDeliverBase best = findBest(pos, delivers);
        if (VassalUtils.isNotNull(best, LOGGER))
        {
            setDeliverTask((TaskDeliverBase)TaskBase.createTask(_entityAI, best));
            setResolving(Resolving.UNRESOLVED);
            return true;
        }

        return false;
    }

    private boolean linkupAcquireResponses(List<MessageTaskRequest> responses)
    {
        //debugLog("Linking up acquire responses.");
        BlockPos pos = _entityAI.getBlockPos();

        List<TRAcquireBase> acquires = MessageUtils.extractMessages(this, responses, TRAcquireBase.class);
        //LOGGER.debug("Have (" + acquires.size() + ") acquires ");
        if (acquires.size() > 0)
        {
            TRAcquireBase best = findBest(pos, acquires);
            //LOGGER.debug("Best acquire: " + best);
            if (VassalUtils.isNotNull(best, LOGGER))
            {
                setAcquireTask((TaskAcquireBase)TaskBase.createTask(_entityAI, best));
                setResolving(Resolving.UNRESOLVED);
                return true;
            }
        }
        return false;
    }

    private <T extends MessageTaskRequest> T findBest(BlockPos pos, List<T> responses)
    {
        T best = null;
        int bestValue = Integer.MIN_VALUE;

        // Move helper
        for (T msg : responses)
        {
            if (!_entityAI.canGetTo(msg.getSender().getBlockPos()))
                continue;

            int value = msg.getValue() - Priority.computeDistanceCost(pos, msg.getSender().getBlockPos(),
                    _entityAI.getEntity().getSpeedFactor());
            LOGGER.debug("findBest: task=" + this + ", cost=" + value + ", msg=" + msg);
            if (value > bestValue)
            {
                bestValue = value;
                best = msg;
            }
        }
        return best;
    }

    @Override
    public String toString()
    {
        return VassalUtils.objID(this) + "{" +
                "stage=" + _stage.name() +
                ", resolving=" + _resolving.name() +
                ", current=" + (_current != null) +
                ", acquireTask=" + VassalUtils.getSimpleName(_acquireTask) +
                ", deliverTask=" + VassalUtils.getSimpleName(_deliverTask) +
                '}';
    }


    // Back reference to the entityAI.
    private final EntityAIVassal    _entityAI;
    private boolean                 _repeat;

    private Stage                   _stage = Stage.EMPTYING;
    private Resolving               _resolving = Resolving.UNRESOLVED;

    private TaskBase                _current;

    private TaskAcquireBase         _acquireTask;
    private TaskDeliverBase         _deliverTask;


    private static final Logger LOGGER = LogManager.getLogger();
}
