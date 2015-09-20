package com.crashbox.vassal.messaging;

import com.crashbox.vassal.common.ItemStackMatcher;
import com.crashbox.vassal.task.TaskAcquireBase;
import com.crashbox.vassal.task.TaskQuarry;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class TRQuarry extends TRAcquireBase
{
    public TRQuarry(IMessager sender, IMessager target, Object transactionID, int value,
            ItemStackMatcher matcher, int quantity)
    {
        super(sender, target, transactionID, value, TaskQuarry.class, matcher, quantity);
    }
}
