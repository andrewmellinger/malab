package com.crashbox.malab.messaging;

import com.crashbox.malab.common.ItemStackMatcher;
import com.crashbox.malab.task.TaskPutInInventory;

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
