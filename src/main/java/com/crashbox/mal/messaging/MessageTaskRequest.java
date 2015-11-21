package com.crashbox.mal.messaging;

import com.crashbox.mal.task.TaskBase;

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
     * @param value The priority of the task.
     * @param clazz The task to perform.
     */
    public MessageTaskRequest(IMessager sender, IMessager target, Object transactionID, int value,
            Class<? extends TaskBase> clazz)
    {
        super(sender, target, transactionID, value);
        _taskClass = clazz;
    }

    public Class<? extends TaskBase> getTaskClass()
    {
        return _taskClass;
    }

    public void debugInfo(StringBuilder builder)
    {
        super.debugInfo(builder);
        builder.append(", _taskClass=").append(_taskClass.getSimpleName());
    }

    private final Class<? extends TaskBase> _taskClass;
}
