package com.crashbox.drudgemod.messaging;

import com.crashbox.drudgemod.ai.EntityAIDrudge;
import com.crashbox.drudgemod.messaging.Message;
import net.minecraft.item.ItemStack;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class MessageItemRequest extends Message
{
    public MessageItemRequest(EntityAIDrudge drudge, ItemStack itemSample, int quantity)
    {
        super(drudge, null);
        _drudgeAI = drudge;
        _itemSample = itemSample;
        _quantity = quantity;
    }

    public ItemStack getItemSample()
    {
        return _itemSample;
    }

    public EntityAIDrudge getAIDrudge()
    {
        return _drudgeAI;
    }

    public int getQuantity()
    {
        return _quantity;
    }

    @Override
    public String toString()
    {
        return "MessageItemRequest{" +
                "_drudgeAI=" + Integer.toHexString(_drudgeAI.hashCode()) +
                ", _itemSample=" + _itemSample +
                ", _quantity=" + _quantity +
                '}';
    }

    private final EntityAIDrudge _drudgeAI;
    private final ItemStack _itemSample;
    private final int _quantity;
}
