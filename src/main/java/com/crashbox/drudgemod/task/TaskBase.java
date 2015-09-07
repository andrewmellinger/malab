package com.crashbox.drudgemod.task;

import com.crashbox.drudgemod.DrudgeUtils;
import com.crashbox.drudgemod.EntityDrudge;
import com.crashbox.drudgemod.ai.EntityAIDrudge;
import com.crashbox.drudgemod.messaging.IMessager;
import com.crashbox.drudgemod.messaging.Message;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Copyright 2015 Andrew o. Mellinger
 */
public abstract class TaskBase
{
    public enum Resolving { UNRESOLVED, RESOLVING, RESOLVED }
    public enum State { INPROGRESS, SUCCESS, FAILED }

    /**
     * Base class for all tasks.
     * @param performer The AI performing this task
     * @param requester This is the requester of the task.
     * @param priority The priority of the task.
     */
    protected TaskBase(EntityAIDrudge performer, IMessager requester, int priority)
    {
        _performer = performer;
        _requester = requester;
        _priority = priority;
    }

    public IMessager getRequester()
    {
        return _requester;
    }

    public EntityDrudge getEntity()
    {
        return _performer.getEntity();
    }

    public EntityAIDrudge getPerformer()
    {
        return _performer;
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
        return _state == State.INPROGRESS;
    }

    public abstract void resetTask();

    public abstract void updateTask();

    //=============================================================================================

    // Make sure it fixes pre-requisites
    public abstract Message resolve();

    // The value.  The higher the number the better.
    public abstract int getValue();

    // The task will figure out exactly where to go
    public abstract BlockPos selectWorkArea(List<BlockPos> others);

    //=============================================================================================


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

    public Resolving getResolving()
    {
        return _resolving;
    }

    public void setResolving(Resolving resolving)
    {
        _resolving = resolving;
    }

    public State getState()
    {
        return _state;
    }

    public void setState(State state)
    {
        _state = state;
    }



    public World getWorld()
    {
        return _performer.getEntity().getEntityWorld();
    }

    // CONVENIENCES

    // Convenience method
    public boolean tryMoveTo(BlockPos pos)
    {
        return getPerformer().tryMoveTo(pos);
    }


    public void debugLog(Logger logger, String message)
    {
        logger.debug(getPerformer().id() + " " + message);
    }


    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append(this.getClass().getSimpleName());
        builder.append("@");
        builder.append(Integer.toHexString(this.hashCode()));
        builder.append("{");
        debugInfo(builder);
        builder.append("}");
        return builder.toString();
    }

    public void debugInfo(StringBuilder builder)
    {
        builder.append("performer=").append(DrudgeUtils.objID(_requester));
        builder.append(", requester=").append(DrudgeUtils.objID(_requester));
        builder.append(", priority=").append(_priority);
        builder.append(", nextTask=").append(DrudgeUtils.objID(_nextTask));
        builder.append(", resolving=").append(_resolving);
    }



    public State _state = State.INPROGRESS;

    // Who is executing the task?
    protected final EntityAIDrudge _performer;

    // Who generated the task
    protected final IMessager _requester;

    // The priority of the task.
    protected final int _priority;

    // Use for chaining
    protected TaskBase _nextTask;

    private Resolving _resolving = Resolving.UNRESOLVED;

    private static final Logger LOGGER = LogManager.getLogger();
}
