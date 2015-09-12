package com.crashbox.drudgemod.task;

import com.crashbox.drudgemod.ai.EntityAIDrudge;
import com.crashbox.drudgemod.messaging.IMessager;
import net.minecraft.item.ItemStack;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public abstract class TaskAcquireBase extends TaskBase
{
    public TaskAcquireBase(EntityAIDrudge performer, IMessager requester, int priority)
    {
        super(performer, requester, priority);
    }

    /**
     * @return Sample item stack showing what we'll provide.
     */
    public ItemStack getSample()
    {
        return _sample;
    }

    protected ItemStack _sample;
}
