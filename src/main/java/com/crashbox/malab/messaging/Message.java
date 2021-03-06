package com.crashbox.malab.messaging;

import com.crashbox.malab.ai.EntityAIWorkDroid;
import com.crashbox.malab.util.MALUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
     * @param value The value executing the task provides.
     */
    protected Message(IMessager sender, IMessager target, Object transactionID, int value)
    {
        _sender = sender;
        _target = target;
        _transactionID = transactionID;
        _value = value;
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

    public int getValue()
    {
        return _value;
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
        String sender = MALUtils.objID(_sender);
        if (_sender instanceof EntityAIWorkDroid)
            sender = ((EntityAIWorkDroid)_sender).getEntity().getCustomNameTag();

        String target = MALUtils.objID(_target);
        if (_target instanceof EntityAIWorkDroid)
            target = ((EntityAIWorkDroid)_target).getEntity().getCustomNameTag();

        builder.append(" sender=").append(sender);
        builder.append(", target=").append(target);
        builder.append(", transactionID=").append(MALUtils.objID(_transactionID));
        builder.append(", priority=").append(_value);
    }

    private final IMessager _sender;
    private final IMessager _target;
    private final Object _transactionID;
    private final int _value;

    private static final Logger LOGGER = LogManager.getLogger();
}
