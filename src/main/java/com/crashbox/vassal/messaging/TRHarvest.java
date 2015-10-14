package com.crashbox.vassal.messaging;

import com.crashbox.vassal.common.ItemStackMatcher;
import com.crashbox.vassal.task.TaskHarvest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class TRHarvest extends TRAcquireBase
{
    public TRHarvest(IMessager sender, IMessager target, Object transactionID, int value,
                     Class<? extends TaskHarvest>  clazz, ItemStackMatcher matcher, int quantity)
    {
        super(sender, target, transactionID, value, clazz, matcher, quantity);
    }

    private static final Logger LOGGER = LogManager.getLogger();
}
