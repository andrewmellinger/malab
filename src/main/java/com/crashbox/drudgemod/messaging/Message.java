package com.crashbox.drudgemod.messaging;

/**
 * Copyright 2015 Andrew o. Mellinger
 */
public class Message
{
    public Message(IMessageSender sender)
    {
        _sender = sender;
    }

    public IMessageSender getSender()
    {
        return _sender;
    }

    private IMessageSender _sender;
}
