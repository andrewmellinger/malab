package com.crashbox.drudgemod.messaging;

import com.crashbox.drudgemod.common.ItemStackMatcher;
import com.crashbox.drudgemod.task.TaskStore;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class MessageStoreRequest extends MessageDeliverRequest
{
    public MessageStoreRequest(IMessager sender, IMessager receiver, Object transactionID, int priority,
            ItemStackMatcher matcher, int quantity, int slot)
    {
        super(sender, receiver, transactionID, priority, TaskStore.class, matcher, quantity);
        _slot = slot;
    }

    public int getSlot()
    {
        return _slot;
    }

    public void debugInfo(StringBuilder builder)
    {
        super.debugInfo(builder);
        builder.append(", slot=").append(_slot);
    }


    private final int _slot;
}
