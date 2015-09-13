package com.crashbox.drudgemod.messaging;

import com.crashbox.drudgemod.common.ItemStackMatcher;
import com.crashbox.drudgemod.task.TaskBase;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class TRAcquireBase extends MessageTaskRequest
{
    public TRAcquireBase(IMessager sender, IMessager target, Object transactionID, int priority,
            Class<? extends TaskBase> clazz, ItemStackMatcher matcher,
            int quantity)
    {
        super(sender, target, transactionID, priority, clazz);
        _matcher = matcher;
        _quantity = quantity;
    }

    public ItemStackMatcher getMatcher()
    {
        return _matcher;
    }

    public int getQuantity()
    {
        return _quantity;
    }

    @Override
    public void debugInfo(StringBuilder builder)
    {
        super.debugInfo(builder);
        builder.append(", sample=").append(_matcher);
        builder.append(", quantity=").append(_quantity);
    }

    private final ItemStackMatcher _matcher;
    private final int _quantity;
}
