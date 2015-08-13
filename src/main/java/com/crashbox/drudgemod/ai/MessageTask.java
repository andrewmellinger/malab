package com.crashbox.drudgemod.ai;

import com.crashbox.drudgemod.messaging.Message;
import com.crashbox.drudgemod.messaging.IMessageSender;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class MessageTask extends Message<TaskBase>
{
    public MessageTask(IMessageSender sender, TaskBase payload)
    {
        super(sender, payload);
    }
}
