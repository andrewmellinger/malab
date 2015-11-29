package com.crashbox.malab.messaging;

import com.crashbox.malab.common.ItemStackMatcher;
import com.crashbox.malab.task.TaskGetFromInventory;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class TRGetFromInventory extends TRAcquireBase
{
    public TRGetFromInventory(IMessager sender, IMessager target, Object transactionID, int priority,
            ItemStackMatcher matcher, int quantity)
    {
        super(sender, target, transactionID, priority, TaskGetFromInventory.class, matcher, quantity);
    }
}
