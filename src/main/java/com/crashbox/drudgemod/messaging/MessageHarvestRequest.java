package com.crashbox.drudgemod.messaging;

import com.crashbox.drudgemod.DrudgeUtils;
import com.crashbox.drudgemod.task.TaskHarvest;
import net.minecraft.item.ItemStack;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class MessageHarvestRequest extends MessageTaskRequest
{
    public MessageHarvestRequest(IMessager sender, IMessager target, Object cause, int priority, ItemStack sample,
            int quantity)
    {
        super(sender, target, cause, priority, TaskHarvest.class);
        _sample = sample;
        _quantity = quantity;
    }

    public ItemStack getSample()
    {
        return _sample;
    }

    public int getQuantity()
    {
        return _quantity;
    }

    @Override
    public void debugInfo(StringBuilder builder)
    {
        super.debugInfo(builder);
        builder.append(", sample=").append(_sample);
        builder.append(", quantity=").append(_quantity);
    }

    private final ItemStack _sample;
    private final int _quantity;
}
