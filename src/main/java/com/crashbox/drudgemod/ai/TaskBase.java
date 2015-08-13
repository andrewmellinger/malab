package com.crashbox.drudgemod.ai;

import com.crashbox.drudgemod.EntityDrudge;
import net.minecraft.util.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Copyright 2015 Andrew o. Mellinger
 */
public abstract class TaskBase
{
    protected TaskBase(TaskMaster tasker)
    {
        this(tasker, null);
    }

    protected TaskBase(TaskMaster tasker, BlockPos focusBlock)
    {
        _tasker = tasker;
        _focusBlock = focusBlock;
    }

    public BlockPos getFocusBlock()
    {
        return _focusBlock;
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
    public abstract boolean continueExecution();

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
        _tasker.taskCompleted(this);
        _entityAI = null;
    }

    public boolean isAccepted()
    {
        return (_accepted != 0);
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

    // Who is executing the task?   Can be null.
    protected EntityAIDrudge _entityAI;

    // When was the task accepted?  0 for not accepted.
    private long _accepted = 0;

    private static final Logger LOGGER = LogManager.getLogger();
}
