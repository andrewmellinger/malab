package com.crashbox.malab.task;

import com.crashbox.malab.util.MALUtils;
import com.crashbox.malab.workdroid.EntityWorkDroid;
import com.crashbox.malab.ai.EntityAIWorkDroid;
import com.crashbox.malab.messaging.IMessager;
import com.crashbox.malab.messaging.MessageTaskRequest;
import com.crashbox.malab.task.ITask.UpdateResult;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.util.List;

/**
 * Copyright 2015 Andrew o. Mellinger
 */
public abstract class TaskBase
{
    public static TaskBase createTask(EntityAIWorkDroid performer, MessageTaskRequest message)
    {
        Class<? extends TaskBase> clazz = message.getTaskClass();
        try
        {
            Constructor<? extends TaskBase> ctor = clazz.getConstructor(EntityAIWorkDroid.class,
                    message.getClass());
            TaskBase task = ctor.newInstance(performer, message);
            return task;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            LOGGER.error("Failed to construct task from msg=" + message, e);
            throw new IllegalArgumentException("Failed to construct task from msg=" + message, e);
        }
    }

    /**
     * Base class for all tasks.
     * @param performer The AI performing this task
     * @param requester This is the requester of the task.
     * @param value The priority of the task.
     */
    protected TaskBase(EntityAIWorkDroid performer, IMessager requester, int value)
    {
        _performer = performer;
        _requester = requester;
        _value = value;
    }

    public IMessager getRequester()
    {
        return _requester;
    }

    public EntityWorkDroid getEntity()
    {
        return _performer.getEntity();
    }

    public EntityAIWorkDroid getPerformer()
    {
        return _performer;
    }

    /**
     * @return The value of this work being performed.
     */
    public int getValue()
    {
        return _value;
    }

    // =======================
    // TASK SPECIFICS

    /**
     * @return The general position we move towards.
     */
    public BlockPos getWorkCenter()
    {
        return getRequester().getBlockPos();
    }

    /**
     * This is where the bot needs to move to do executeAndIsDone the work.  This is not necessarily
     * the actual block.
     * @param others Other people working near the requester.
     * @return The actual work center we want to work at.
     */
    public abstract BlockPos getWorkTarget(List<BlockPos> others);

    /**
     * Make progress on the work indicated when we are done with this target block.
     * @return True if we are done.
     */
    public abstract UpdateResult executeAndIsDone();


    //=============================================================================================
    // Conveniences

    public World getWorld()
    {
        return _performer.getEntity().getEntityWorld();
    }

    //=============================================================================================
    // LOGGING

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
        builder.append("performer=").append(MALUtils.objID(_requester));
        builder.append(", requester=").append(MALUtils.objID(_requester));
        builder.append(", value=").append(_value);
    }

    //=============================================================================================
    protected void setDone(boolean done)
    {
        _done = done;
    }

    // Who is executing the task?
    protected final EntityAIWorkDroid _performer;

    // Who generated the task
    protected final IMessager _requester;

    // The priority of the task.
    protected final int _value;

    // Has the task met its threshold, or run out of capability?
    private boolean _done = false;

    private static final Logger LOGGER = LogManager.getLogger();

}
