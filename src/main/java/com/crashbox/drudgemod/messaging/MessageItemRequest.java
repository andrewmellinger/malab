package com.crashbox.drudgemod.messaging;

import com.crashbox.drudgemod.task.TaskBase;
import com.crashbox.drudgemod.task.TaskDeliver;
import net.minecraft.item.ItemStack;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class MessageItemRequest extends  Message
{
    // TODO: Item request don't go to drudges
    public MessageItemRequest(IMessager sender, IMessager receiver, Object cause, ItemStack itemSample, int quantity)
    {
        super(sender, receiver, cause, 0);
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

    public void debugInfo(StringBuilder builder)
    {
        super.debugInfo(builder);
        builder.append(", _itemSample=").append(_itemSample);
        builder.append(", _quantity=").append(_quantity);
    }

    private final ItemStack _itemSample;
    private final int _quantity;
}
