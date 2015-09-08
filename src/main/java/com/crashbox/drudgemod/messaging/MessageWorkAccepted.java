package com.crashbox.drudgemod.messaging;

import com.crashbox.drudgemod.DrudgeUtils;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class MessageWorkAccepted extends Message
{
    public MessageWorkAccepted(IMessager sender, IMessager target, Object transactionID, int priority, int delayMS)
    {
        super(sender, target, transactionID, priority);
        _delayMS = delayMS;
    }

    public int getDelayMS()
    {
        return _delayMS;
    }

    public void debugInfo(StringBuilder builder)
    {
        super.debugInfo(builder);
        builder.append(", delay=").append(_delayMS);
    }

    private final int _delayMS;
}
