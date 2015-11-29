package com.crashbox.malab.task;

import com.crashbox.malab.ai.EntityAIWorkDroid;
import com.crashbox.malab.common.ItemStackMatcher;
import com.crashbox.malab.messaging.IMessager;
import net.minecraft.item.ItemStack;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public abstract class TaskAcquireBase extends TaskBase
{
    public TaskAcquireBase(EntityAIWorkDroid performer, IMessager requester, int priority, ItemStackMatcher matcher)
    {
        super(performer, requester, priority);
        _matcher = matcher;
    }

    public ItemStackMatcher getMatcher()
    {
        ItemStack held = getPerformer().getEntity().getHeldItem();
        if (held != null)
            return new ItemStackMatcher(held);
        // We should
        return _matcher;
    }

    public void debugInfo(StringBuilder builder)
    {
        super.debugInfo(builder);
        builder.append(", matcher=").append(_matcher);
    }

    private final ItemStackMatcher _matcher;
}
