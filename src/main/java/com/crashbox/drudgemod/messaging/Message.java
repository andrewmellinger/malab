package com.crashbox.drudgemod.messaging;

import com.crashbox.drudgemod.DrudgeUtils;

/**
 * Copyright 2015 Andrew o. Mellinger
 */
public class Message
{
    /**
     * Base class for messages.
     * @param sender Who sent the message.
     * @param target Intended recipient, null for broadcast.
     * @param transactionID Reason we sent this.  Used for linking return messages.
     * @param priority
     */
    protected Message(IMessager sender, IMessager target, Object transactionID, int priority)
    {
        _sender = sender;
        _target = target;
        _transactionID = transactionID;
        _priority = priority;
    }

    public IMessager getSender()
    {
        return _sender;
    }

    public IMessager getTarget()
    {
        return _target;
    }

    public Object getTransactionID()
    {
        return _transactionID;
    }

    public int getPriority()
    {
        return _priority;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append(this.getClass().getSimpleName());
        builder.append("@");
        builder.append(Integer.toHexString(this.hashCode()));
        builder.append("{");
        debugInfo(builder);
        builder.append("}");
        return builder.toString();
    }

    public void debugInfo(StringBuilder builder)
    {
        builder.append(" sender=").append(DrudgeUtils.objID(_sender));
        builder.append(", target=").append(DrudgeUtils.objID(_target));
        builder.append(", transactionID=").append(DrudgeUtils.objID(_transactionID));
        builder.append(", priority=").append(_priority);
    }

    private final IMessager _sender;
    private final IMessager _target;
    private final Object _transactionID;
    private final int _priority;
}
