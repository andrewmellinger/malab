package com.crashbox.vassal.messaging;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class MessageWorkingHeartbeat extends Message
{
    public MessageWorkingHeartbeat(IMessager sender, IMessager target, Object transactionID, long expireMS)
    {
        super(sender, target, transactionID, 0);
        _expireMS = expireMS;
    }

    public long getExpireMS()
    {
        return _expireMS;
    }

    public void debugInfo(StringBuilder builder)
    {
        super.debugInfo(builder);
        builder.append(", expire=").append(_expireMS);
    }

    private final long _expireMS;
}
