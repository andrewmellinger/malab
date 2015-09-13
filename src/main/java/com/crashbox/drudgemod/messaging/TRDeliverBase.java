package com.crashbox.drudgemod.messaging;

import com.crashbox.drudgemod.common.ItemStackMatcher;
import com.crashbox.drudgemod.task.TaskDeliverBase;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class TRDeliverBase extends MessageTaskRequest
{
    public TRDeliverBase(IMessager sender, IMessager receiver, Object transactionID, int priority,
            Class<? extends TaskDeliverBase> clazz, ItemStackMatcher matcher, int quantity)
    {
        super(sender, receiver, transactionID, priority, clazz);
        if (matcher == null)
            throw new IllegalArgumentException("Matcher must not be null.");
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


    public void debugInfo(StringBuilder builder)
    {
        super.debugInfo(builder);
        builder.append(", itemSample=").append(_matcher);
        builder.append(", quantity=").append(_quantity);
    }

    private final ItemStackMatcher _matcher;
    private final int _quantity;

}
