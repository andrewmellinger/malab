package com.crashbox.drudgemod.ai;

import com.crashbox.drudgemod.EntityDrudge;
import com.crashbox.drudgemod.messaging.Message;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Copyright 2015 Andrew o. Mellinger
 */
public abstract class TaskBase
{
    /**
     * Base class for all tasks.
     * @param tasker This is the TaskMaster who originated the tasks.
     * @param focusBlock The block we are focused on.
     * @param priority The priority of the task.
     */
    protected TaskBase(TaskMaster tasker, BlockPos focusBlock, int priority)
    {
        _tasker = tasker;
        _focusBlock = focusBlock;
        _priority = priority;
    }


    @Deprecated
    public BlockPos getFocusBlock()
    {
        return _focusBlock;
    }

    /**
     * Used to indicate if a task is focused on this block.  If true, other tasks of the same
     * type should not focus on ths block.
     * @param pos The position in question
     * @return True if this task is focused on the pos.
     */
    public boolean isFocusBlock(BlockPos pos)
    {
        return false;
    }

    /**
     * Called when accepted by a drudge.  This keeps track of who accepted and when.
     * @param entityAI The entity who accepted.
     */
    public void accept(EntityAIDrudge entityAI)
    {
        _entityAI = entityAI;
        _accepted = System.currentTimeMillis();
    }

    public EntityDrudge getEntity()
    {
        return _entityAI.getEntity();
    }

    // =======================
    // TASK SPECIFICS

    /**
     * Called by the entity who is doing the task when it should execute.
     */
    public abstract void execute();

    /**
     * @return True if we are doing work.
     */
    public boolean continueExecution()
    {
        return !_complete;
    }

    public abstract void resetTask();

    public abstract void updateTask();

    /**
     * @return The next task for task chaining
     */
    public TaskBase getNextTask()
    {
        return _nextTask;
    }

    /**
     * Sets the next task
     */
    public void setNextTask(TaskBase task)
    {
        _nextTask = task;
    }

    // =======================
    // Task Acceptance

    // Called if not accepted
    public void reject()
    {
        LOGGER.debug("Task rejected.");
        _tasker.taskRejected(this);
    }

    // Called when completed
    public void complete()
    {
        LOGGER.debug("Task completed.");
        _complete = true;
        _tasker.taskCompleted(this);
        _entityAI = null;
    }

    public boolean isAccepted()
    {
        return (_accepted != 0);
    }

    public boolean isComplete()
    {
        return _complete;
    }

    public World getWorld()
    {
        return _entityAI.getEntity().getEntityWorld();
    }

    // Convenience method
    public boolean tryMoveTo(BlockPos pos)
    {
        return getEntity().getNavigator().tryMoveToXYZ(pos.getX(), pos.getY(), pos.getZ(), getEntity().getSpeed());
    }


    @Override
    public String toString()
    {
        return "TaskBase{" +
                "_tasker=" + _tasker +
                ", _focusBlock=" + _focusBlock +
                ", _accepted=" + _accepted +
                '}';
    }

    // Who generated the task
    protected final TaskMaster _tasker;

    // Is the task focused on a particular block such as harvesting?
    protected BlockPos _focusBlock;

    protected final int _priority;

    // Who is executing the task?   Can be null.
    protected EntityAIDrudge _entityAI;

    // Use for chaining
    protected TaskBase _nextTask;

    // When was the task accepted?  0 for not accepted.
    private long _accepted = 0;
    private boolean _complete = false;

    private static final Logger LOGGER = LogManager.getLogger();
}
