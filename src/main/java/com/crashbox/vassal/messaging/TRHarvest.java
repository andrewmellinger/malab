package com.crashbox.vassal.messaging;

import com.crashbox.vassal.common.ItemStackMatcher;
import com.crashbox.vassal.task.TaskHarvest;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class TRHarvest extends TRAcquireBase
{
    public TRHarvest(IMessager sender, IMessager target, Object transactionID, int priority,
                     Class<? extends TaskHarvest>  clazz, ItemStackMatcher matcher, int quantity)
    {
        super(sender, target, transactionID, priority, clazz, matcher, quantity);
    }
}
