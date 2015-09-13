package com.crashbox.drudgemod.messaging;

import com.crashbox.drudgemod.DrudgeUtils;
import com.crashbox.drudgemod.task.TaskAcquireBase;
import com.crashbox.drudgemod.task.TaskDeliverBase;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class MessageTaskPairRequest extends Message
{
    public MessageTaskPairRequest(IMessager sender, IMessager target, Object transactionID, boolean repeat,
            MessageAcquireRequest acquire, MessageDeliverRequest deliver)
    {
        super(sender, target, transactionID, 0);
        _repeat = repeat;
        _acquire = acquire;
        _deliver = deliver;
    }

    /** Should we repeat the task until acquire runs out. */
    public boolean getRepeat()
    {
        return _repeat;
    }

    public MessageAcquireRequest getAcquireRequest()
    {
        return _acquire;
    }

    public MessageDeliverRequest getDeliverRequest()
    {
        return _deliver;
    }

    private final boolean _repeat;
    private final MessageAcquireRequest _acquire;
    private final MessageDeliverRequest _deliver;
}
