package com.crashbox.drudgemod.messaging;

import com.crashbox.drudgemod.task.TaskDeliver;
import net.minecraft.item.ItemStack;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class MessageDeliverRequest extends MessageTaskRequest
{
    public MessageDeliverRequest(IMessager sender, IMessager receiver, Object cause, int priority,
            ItemStack itemSample, int quantity, int slot)
    {
        super(sender, receiver, cause, priority, TaskDeliver.class);
        _itemSample = itemSample;
        _quantity = quantity;
        _slot = slot;
    }

    public ItemStack getItemSample()
    {
        return _itemSample;
    }

    public int getQuantity()
    {
        return _quantity;
    }

    public int getSlot()
    {
        return _slot;
    }

    public void debugInfo(StringBuilder builder)
    {
        super.debugInfo(builder);
        builder.append(", itemSample=").append(_itemSample);
        builder.append(", quantity=").append(_quantity);
        builder.append(", slot=").append(_slot);
    }

    private final ItemStack _itemSample;
    private final int _quantity;
    private final int _slot;
}
