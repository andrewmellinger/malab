package com.crashbox.drudgemod.task;

import com.crashbox.drudgemod.ai.EntityAIDrudge;
import com.crashbox.drudgemod.common.ItemStackMatcher;
import com.crashbox.drudgemod.messaging.IMessager;
import net.minecraft.item.Item;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public abstract class TaskDeliverBase extends TaskBase
{
    public TaskDeliverBase(EntityAIDrudge performer, IMessager requester, int priority)
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
