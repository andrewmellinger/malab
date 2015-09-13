package com.crashbox.drudgemod.task;

import com.crashbox.drudgemod.DrudgeUtils;
import com.crashbox.drudgemod.EntityDrudge;
import com.crashbox.drudgemod.ai.EntityAIDrudge;
import com.crashbox.drudgemod.ai.Priority;
import com.crashbox.drudgemod.messaging.IMessager;
import com.crashbox.drudgemod.messaging.Message;
import com.crashbox.drudgemod.messaging.MessageTaskRequest;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Copyright 2015 Andrew o. Mellinger
 */
public abstract class TaskBase
{
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

    public boolean isDone()
    {
        return _done;
    }

    // =======================
    // TASK SPECIFICS

    /**
     * @return The general position we move towards.
     */
    public BlockPos getCoarsePos()
    {
        return getRequester().getPos();
    }

    /**
     * This is where the bot needs to move to do executeAndIsDone the work.  This is not necessarily
     * the actual block.
     * @param others Other people working near the requester.
     * @return The actual work center we want to work at.
     */
    public abstract BlockPos chooseWorkArea(List<BlockPos> others);

    /**
     * Make progress on the work indicated when we are done with this target block.
     * @return True if we are done.
     */
    public abstract boolean executeAndIsDone();

    /**
     * @return The value of this work being performed.
     */
    public abstract int getValue();

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
        builder.append("performer=").append(DrudgeUtils.objID(_requester));
        builder.append(", requester=").append(DrudgeUtils.objID(_requester));
        builder.append(", priority=").append(_priority);
    }

    //=============================================================================================

    // Utility Methods

    public static List<MessageTaskRequest> getAllForTask(List<MessageTaskRequest> responses, TaskBase task)
    {
        List<MessageTaskRequest> result = new ArrayList<MessageTaskRequest>();

        Iterator<MessageTaskRequest> iterator = responses.iterator();
        while (iterator.hasNext())
        {
            MessageTaskRequest msg =  iterator.next();
            if (msg.getTransactionID() == task)
            {
                iterator.remove();
                result.add(msg);
            }
        }

        return result;
    }

    public static MessageTaskRequest findBestResponseOption(TaskBase task, List<MessageTaskRequest> messages)
    {
        int bestValue = Integer.MIN_VALUE;
        MessageTaskRequest bestTask = null;

        for (MessageTaskRequest request : messages)
        {
            int value = request.getValue() - Priority.computeDistanceCost(request.getSender().getPos(),
                    task.getRequester().getPos());
            if (value > bestValue)
                bestTask = request;
        }

        return bestTask;
    }


    //=============================================================================================
    protected void setDone(boolean done)
    {
        _done = done;
    }

    // Who is executing the task?
    protected final EntityAIDrudge _performer;

    // Who generated the task
    protected final IMessager _requester;

    // The priority of the task.
    protected final int _priority;

    // Has the task met its threshold, or run out of capability?
    private boolean _done = false;

    private static final Logger LOGGER = LogManager.getLogger();
}
