package com.crashbox.drudgemod.ai;

import com.crashbox.drudgemod.messaging.Message;
import com.crashbox.drudgemod.messaging.IMessager;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class MessageTask extends Message
{
    public MessageTask(IMessager sender, TaskBase task)
    {
        super(sender, null);
        _task = task;
    }

    private final TaskBase _task;
}
