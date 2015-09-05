package com.crashbox.drudgemod.messaging;

import com.crashbox.drudgemod.task.TaskBase;
import com.crashbox.drudgemod.task.TaskDeliver;
import net.minecraft.item.ItemStack;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class MessageItemRequest extends  MessageTaskRequest
{
    public MessageItemRequest(IMessager sender, IMessager receiver, ItemStack itemSample, int quantity)
    {
        super(sender, receiver, TaskDeliver.class, 0);
        _itemSample = itemSample;
        _quantity = quantity;
    }

    public MessageItemRequest(IMessager sender, IMessager receiver, Class<? extends TaskBase> clazz, int priority,
            ItemStack itemSample, int quantity)
    {
        super(sender, receiver, clazz, priority);
        _itemSample = itemSample;
        _quantity = quantity;
    }

    public ItemStack getItemSample()
    {
        return _itemSample;
    }

    public int getQuantity()
    {
        return _quantity;
    }

    @Override
    public String toString()
    {
        return "MessageDeliverRequest{" +
                ", _itemSample=" + _itemSample +
                ", _quantity=" + _quantity +
                '}';
    }

    private final ItemStack _itemSample;
    private final int _quantity;
}
