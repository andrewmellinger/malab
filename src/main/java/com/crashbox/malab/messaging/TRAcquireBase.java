package com.crashbox.malab.messaging;

import com.crashbox.malab.common.ItemStackMatcher;
import com.crashbox.malab.task.TaskAcquireBase;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class TRAcquireBase extends MessageTaskRequest
{
    public TRAcquireBase(IMessager sender, IMessager target, Object transactionID, int value,
            Class<? extends TaskAcquireBase> clazz, ItemStackMatcher matcher, int quantity)
    {
        super(sender, target, transactionID, value, clazz);
        _matcher = matcher;
        _quantity = quantity;

        if (matcher == null)
            throw new IllegalArgumentException("Matcher must not be null.");
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
