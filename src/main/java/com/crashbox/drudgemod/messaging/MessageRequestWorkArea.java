package com.crashbox.drudgemod.messaging;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class MessageRequestWorkArea extends MessageDataRequest
{
    public MessageRequestWorkArea(IMessager sender, IMessager target, Object transactionID, int priority)
    {
        super(sender, target, transactionID, priority);
    }
}
