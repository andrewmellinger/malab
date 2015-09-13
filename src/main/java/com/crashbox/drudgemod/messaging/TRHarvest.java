package com.crashbox.drudgemod.messaging;

import com.crashbox.drudgemod.common.ItemStackMatcher;
import com.crashbox.drudgemod.task.TaskHarvest;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class TRHarvest extends TRAcquireBase
{
    public TRHarvest(IMessager sender, IMessager target, Object transactionID, int priority, ItemStackMatcher matcher,
            int quantity)
    {
        super(sender, target, transactionID, priority, TaskHarvest.class, matcher, quantity);
    }
}
