package com.crashbox.drudgemod.task;

import com.crashbox.drudgemod.ai.EntityAIDrudge;
import com.crashbox.drudgemod.common.ItemStackMatcher;
import com.crashbox.drudgemod.messaging.IMessager;
import net.minecraft.item.ItemStack;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public abstract class TaskAcquireBase extends TaskBase
{
    public TaskAcquireBase(EntityAIDrudge performer, IMessager requester, int priority, ItemStackMatcher matcher)
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
