package com.crashbox.drudgemod.messaging;

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
     * @param clazz The task to perform.
     * @param priority The priority of the task.
     */
    public MessageTaskRequest(IMessager sender, IMessager target, Class<? extends TaskBase> clazz, int priority)
    {
        super(sender, target, null);
        _taskClass = clazz;
        _priority = priority;
    }

    public Class<? extends TaskBase> getTaskClass()
    {
        return _taskClass;
    }

    public int getPriority()
    {
        return _priority;
    }

    private final Class<? extends TaskBase> _taskClass;
    private final int _priority;
}
