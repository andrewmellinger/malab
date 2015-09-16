package com.crashbox.vassal.task;

import com.crashbox.vassal.ai.EntityAIVassal;
import com.crashbox.vassal.common.ItemStackMatcher;
import com.crashbox.vassal.messaging.IMessager;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public abstract class TaskAcquireBase extends TaskBase
{
    public TaskAcquireBase(EntityAIVassal performer, IMessager requester, int priority, ItemStackMatcher matcher)
    {
        super(performer, requester, priority);
        _matcher = matcher;
    }

    public ItemStackMatcher getMatcher()
    {
        return _matcher;
    }

    public void debugInfo(StringBuilder builder)
    {
        super.debugInfo(builder);
        builder.append(", matcher=").append(_matcher);
    }

    protected final ItemStackMatcher _matcher;
}
