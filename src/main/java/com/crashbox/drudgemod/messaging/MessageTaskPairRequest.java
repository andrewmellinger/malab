package com.crashbox.drudgemod.messaging;

import com.crashbox.drudgemod.DrudgeUtils;
import com.crashbox.drudgemod.task.TaskAcquireBase;
import com.crashbox.drudgemod.task.TaskDeliverBase;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class MessageTaskPairRequest extends Message
{
    public MessageTaskPairRequest(IMessager sender, IMessager target, Object transactionID, int value,
            Class<? extends TaskAcquireBase> acquireClass, Class<? extends TaskDeliverBase> deliverClass)
    {
        super(sender, target, transactionID, value);
        _acquireClass = acquireClass;
        _deliverClass = deliverClass;
    }

    public Class<? extends TaskAcquireBase>  getAcquireClass()
    {
        return _acquireClass;
    }

    public Class<? extends TaskDeliverBase> getDeliverClass()
    {
        return _deliverClass;
    }

    public void debugInfo(StringBuilder builder)
    {
        super.debugInfo(builder);
        builder.append(", _acquire=").append(_acquireClass.getSimpleName());
        builder.append(", _deliver=").append(_deliverClass.getSimpleName());
    }

    private final Class<? extends TaskAcquireBase> _acquireClass;
    private final Class<? extends TaskDeliverBase> _deliverClass;
}
