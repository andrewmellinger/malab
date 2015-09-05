package com.crashbox.drudgemod.messaging;

import com.crashbox.drudgemod.task.TaskBase;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class MessageTask extends Message
{
    public MessageTask(IMessager sender, TaskBase task)
    {
        super(sender, null, null);
        _task = task;
    }

    private final TaskBase _task;
}
