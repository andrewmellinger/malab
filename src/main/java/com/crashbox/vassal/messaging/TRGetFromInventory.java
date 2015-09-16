package com.crashbox.vassal.messaging;

import com.crashbox.vassal.common.ItemStackMatcher;
import com.crashbox.vassal.task.TaskGetFromInventory;

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
