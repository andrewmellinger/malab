package com.crashbox.drudgemod.messaging;

/**
 * Copyright 2015 Andrew o. Mellinger
 */
public class Message<T>
{
    public Message(IMessageSender sender, T payload)
    {
        _sender = sender;
        _payload = payload;
    }

    public IMessageSender getSender()
    {
        return _sender;
    }

    public T getPayload()
    {
        return _payload;
    }

    private T _payload;
    private IMessageSender _sender;
}
