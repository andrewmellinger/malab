package com.crashbox.malab.task;

import com.crashbox.malab.ai.EntityAIWorkDroid;
import com.crashbox.malab.common.ItemStackMatcher;
import com.crashbox.malab.messaging.IMessager;

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
