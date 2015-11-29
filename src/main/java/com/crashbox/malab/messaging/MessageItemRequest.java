package com.crashbox.malab.messaging;

import com.crashbox.malab.common.ItemStackMatcher;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class MessageItemRequest extends  Message
{
    public MessageItemRequest(IMessager sender, IMessager receiver, Object transactionID,
                              ItemStackMatcher matcher, int quantity)
    {
        super(sender, receiver, transactionID, 0);
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
        builder.append(", _matcher=").append(_matcher);
        builder.append(", _quantity=").append(_quantity);
    }

    private final ItemStackMatcher _matcher;
    private final int _quantity;
}
