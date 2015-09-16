package com.crashbox.vassal.messaging;

import com.crashbox.vassal.common.ItemStackMatcher;
import com.crashbox.vassal.task.TaskStore;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class TRStore extends TRDeliverBase
{
    public TRStore(IMessager sender, IMessager receiver, Object transactionID, int priority,
            ItemStackMatcher matcher, int quantity)
    {
        super(sender, receiver, transactionID, priority, TaskStore.class, matcher, quantity);
    }

    public void debugInfo(StringBuilder builder)
    {
        super.debugInfo(builder);
    }
}
