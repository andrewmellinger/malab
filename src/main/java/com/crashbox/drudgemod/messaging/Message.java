package com.crashbox.drudgemod.messaging;

/**
 * Copyright 2015 Andrew o. Mellinger
 */
public class Message
{
    public Message(IMessager sender, IMessager target)
    {
        _sender = sender;
        _target = target;
    }

    public IMessager getSender()
    {
        return _sender;
    }

    public IMessager getTarget()
    {
        return _sender;
    }

    private final IMessager _sender;
    private final IMessager _target;
}
