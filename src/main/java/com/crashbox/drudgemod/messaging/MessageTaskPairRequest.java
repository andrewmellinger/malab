package com.crashbox.drudgemod.messaging;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class MessageTaskPairRequest extends Message
{
    public MessageTaskPairRequest(IMessager sender, IMessager target, Object transactionID, boolean repeat,
            TRAcquireBase acquire, TRDeliverBase deliver)
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

    public TRAcquireBase getAcquireRequest()
    {
        return _acquire;
    }

    public TRDeliverBase getDeliverRequest()
    {
        return _deliver;
    }

    private final boolean _repeat;
    private final TRAcquireBase _acquire;
    private final TRDeliverBase _deliver;
}
