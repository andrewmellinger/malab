package com.crashbox.mal.task;

import com.crashbox.mal.ai.EntityAIWorkDroid;
import com.crashbox.mal.common.ItemStackMatcher;
import com.crashbox.mal.messaging.IMessager;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public abstract class TaskDeliverBase extends TaskBase
{
    public TaskDeliverBase(EntityAIWorkDroid performer, IMessager requester, int priority)
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
