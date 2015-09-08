package com.crashbox.drudgemod.messaging;

import com.crashbox.drudgemod.DrudgeUtils;
import com.crashbox.drudgemod.task.TaskBase;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class MessageTaskRequest extends Message
{
    /**
     * A message that wraps a request to perform a task.
     * @param sender Who is sending the request.
     * @param target Who is supposed to perform the request.  Usually a bot.
     * @param transactionID
     * @param priority The priority of the task.
     * @param clazz The task to perform.
     */
    public MessageTaskRequest(IMessager sender, IMessager target, Object transactionID, int priority,
            Class<? extends TaskBase> clazz)
    {
        super(sender, target, transactionID, priority);
        _taskClass = clazz;
    }

    public Class<? extends TaskBase> getTaskClass()
    {
        return _taskClass;
    }

    public void debugInfo(StringBuilder builder)
    {
        super.debugInfo(builder);
        builder.append(", _taskClass=").append(DrudgeUtils.objID(_taskClass));
    }

    private final Class<? extends TaskBase> _taskClass;
}
