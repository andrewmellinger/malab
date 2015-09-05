package com.crashbox.drudgemod.messaging;

/**
 * Copyright 2015 Andrew o. Mellinger
 */
public class Message
{
    /**
     * Base class for messages.
     * @param sender Who sent the message.
     * @param target Intended recipient, null for broadcast.
     * @param cause Reason we sent this.  Used for linking return messages.
     */
    protected Message(IMessager sender, IMessager target, Object cause)
    {
        _sender = sender;
        _target = target;
        _cause = cause;
    }

    public IMessager getSender()
    {
        return _sender;
    }

    public IMessager getTarget()
    {
        return _target;
    }

    public Object getCause()
    {
        return _cause;
    }

    private final IMessager _sender;
    private final IMessager _target;
    private final Object _cause;
}
