package com.crashbox.malab.messaging;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class MessageDataRequest extends Message
{
    public MessageDataRequest(IMessager sender, IMessager target, Object transactionID, int value)
    {
        super(sender, target, transactionID, value);
    }
}
