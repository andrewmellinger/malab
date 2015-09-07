package com.crashbox.drudgemod.messaging;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class MessageRequestWorkArea extends Message
{
    public MessageRequestWorkArea(IMessager sender, IMessager target, Object cause, int priority)
    {
        super(sender, target, cause, priority);
    }
}
