package com.crashbox.drudgemod.ai;

import com.crashbox.drudgemod.messaging.Message;
import com.crashbox.drudgemod.messaging.IMessageSender;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class MessageTask extends Message
{
    public MessageTask(IMessageSender sender, TaskBase task)
    {
        super(sender);
        _task = task;
    }

    private final TaskBase _task;
}
