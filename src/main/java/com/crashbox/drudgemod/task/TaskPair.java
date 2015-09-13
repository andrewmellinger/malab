package com.crashbox.drudgemod.task;

import com.crashbox.drudgemod.ai.EntityAIDrudge;
import net.minecraft.util.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Copyright 2015 Andrew O. Mellinger
 *
 * This is a simple object to track the tasks that make up a set.
 */
public class TaskPair
{
    public enum Resolving { UNRESOLVED, RESOLVING, RESOLVED }
    public enum Stage { EMPTYING, ACQUIRING, DELIVERING, SUCCESS, FAILED}
    public enum UpdateResult { CONTINUE, RETARGET, DONE }

    public TaskPair(EntityAIDrudge entityAI)
    {
        _entityAI = entityAI;
    }

    public TaskDeliverBase getEmptyInventory()
    {
        return _emptyInventory;
    }

    public void setEmptyInventory(TaskDeliverBase emptyInventory)
    {
        _emptyInventory = emptyInventory;
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

    public Resolving getResolving()
    {
        return _resolving;
    }

    public void setResolving(Resolving resolving)
    {
        _resolving = resolving;
    }

    public Stage getStage()
    {
        return _stage;
    }

    public void start()
    {
        if (_emptyInventory != null)
        {
            _current = _emptyInventory;
            _stage = Stage.EMPTYING;
        }
        else if (_acquireTask != null)
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
            _stage = Stage.FAILED;
        }
    }

    public boolean isDone()
    {
        return _stage == Stage.SUCCESS || _stage == Stage.FAILED;
    }

    public BlockPos getWorkCenter()
    {
        return _current.getCoarsePos();
    }

    public BlockPos getWorkTarget(List<BlockPos> exclusions)
    {
        BlockPos workArea = _current.chooseWorkArea(exclusions);

        if (workArea != null)
            return workArea;

        // If we have no work area, we might need to move on.
        if (_stage == Stage.ACQUIRING)
        {
            _stage = Stage.DELIVERING;
            _current = _deliverTask;
        }

        return _current.chooseWorkArea(exclusions);
    }

    public TaskBase[] asList()
    {
        return new TaskBase[] { _emptyInventory, _acquireTask, _deliverTask };
    }

    public UpdateResult updateTask()
    {
        // Keep executing until is says it is done.

        // When done we need to move to the next thing, or we are completely done
        if (!_current.execute())
            return UpdateResult.CONTINUE;

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
                _current = null;
                _stage = Stage.SUCCESS;
                return UpdateResult.DONE;
        }

        return UpdateResult.CONTINUE;
    }

    private boolean acquiredEnough()
    {
        return _entityAI.getEntity().isHeldInventoryFull() ||
               _entityAI.getEntity().getHeldSize() >= _deliverTask.getQuantity();
    }

    @Override
    public String toString()
    {
        return "TaskPair{" +
                "stage=" + _stage.name() +
                ", resolving=" + _resolving.name() +
                ", current=" + (_current != null) +
                ", emptyInventory=" + (_emptyInventory != null) +
                ", acquireTask=" + (_acquireTask != null) +
                ", deliverTask=" + (_deliverTask != null) +
                '}';
    }


    // Back reference to the entityAI.
    private final EntityAIDrudge    _entityAI;

    private Stage                   _stage = Stage.EMPTYING;
    private Resolving               _resolving = Resolving.UNRESOLVED;

    private TaskBase                _current;

    private TaskDeliverBase         _emptyInventory;
    private TaskAcquireBase         _acquireTask;
    private TaskDeliverBase         _deliverTask;


    private static final Logger LOGGER = LogManager.getLogger();
}
