package com.crashbox.drudgemod.messaging;

import com.crashbox.drudgemod.common.ItemStackMatcher;
import com.crashbox.drudgemod.task.TaskHarvest;
import net.minecraft.item.ItemStack;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class MessageHarvestRequest extends MessageTaskRequest
{
    public MessageHarvestRequest(IMessager sender, IMessager target, Object cause, int priority, ItemStackMatcher matcher,
            int quantity)
    {
        super(sender, target, cause, priority, TaskHarvest.class);
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

    @Override
    public void debugInfo(StringBuilder builder)
    {
        super.debugInfo(builder);
        builder.append(", sample=").append(_matcher);
        builder.append(", quantity=").append(_quantity);
    }

    private final ItemStackMatcher _matcher;
    private final int _quantity;
}
