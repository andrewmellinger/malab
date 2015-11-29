package com.crashbox.malab.messaging;

import com.crashbox.malab.common.ItemStackMatcher;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class MessageIsStorageAvailable extends Message
{
    // Used to request a place to store this item
    public MessageIsStorageAvailable(IMessager sender, IMessager target, Object transactionID, int priority,
            ItemStackMatcher matcher)
    {
        super(sender, target, transactionID, priority);
        _matcher = matcher;
    }

    public ItemStackMatcher getMatcher()
    {
        return _matcher;
    }

    @Override
    public void debugInfo(StringBuilder builder)
    {
        super.debugInfo(builder);
        builder.append(", stack=").append(_matcher);
    }

    private final ItemStackMatcher _matcher;
}
