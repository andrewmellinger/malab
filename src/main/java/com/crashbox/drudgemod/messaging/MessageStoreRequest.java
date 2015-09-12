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
        super(sender, receiver, transactionID, priority, TaskStore.class, matcher, quantity, slot);
    }
}
