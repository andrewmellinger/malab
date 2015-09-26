package com.crashbox.vassal.messaging;

import com.crashbox.vassal.common.ItemStackMatcher;
import com.crashbox.vassal.task.TaskPutInInventory;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class TRPutInInventory extends TRDeliverBase
{
    public TRPutInInventory(IMessager sender, IMessager receiver, Object transactionID, int priority,
                            ItemStackMatcher matcher, int quantity)
    {
        super(sender, receiver, transactionID, priority, TaskPutInInventory.class, matcher, quantity);
    }

    public void debugInfo(StringBuilder builder)
    {
        super.debugInfo(builder);
    }
}
