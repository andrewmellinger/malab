package com.crashbox.drudgemod.messaging;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class MessageWorkAccepted extends Message
{
    public MessageWorkAccepted(IMessager sender, IMessager target, Object cause, int priority, int delayMS)
    {
        super(sender, target, cause, priority);
        _delayMS = delayMS;
    }

    public int getDelayMS()
    {
        return _delayMS;
    }

    private final int _delayMS;
}
