package com.crashbox.drudgemod.messaging;

import com.crashbox.drudgemod.task.TaskBase;
import com.crashbox.drudgemod.task.TaskHarvest;
import net.minecraft.item.ItemStack;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class MessageHarvestRequest extends MessageTaskRequest
{
    public MessageHarvestRequest(IMessager sender, IMessager target, int priority, ItemStack sample, int quantity)
    {
        super(sender, target, TaskHarvest.class, priority);
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

    private final ItemStack _sample;
    private final int _quantity;
}
