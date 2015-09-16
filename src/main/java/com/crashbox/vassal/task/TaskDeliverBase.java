package com.crashbox.vassal.task;

import com.crashbox.vassal.ai.EntityAIVassal;
import com.crashbox.vassal.common.ItemStackMatcher;
import com.crashbox.vassal.messaging.IMessager;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public abstract class TaskDeliverBase extends TaskBase
{
    public TaskDeliverBase(EntityAIVassal performer, IMessager requester, int priority)
    {
        super(performer, requester, priority);
    }

    /**
     * @return The matcher that shows what we want.
     */
    public ItemStackMatcher getMatcher()
    {
        return _matcher;
    }

    public int getQuantity()
    {
        return _quantity;
    }

    protected ItemStackMatcher _matcher;
    protected int              _quantity;
}
