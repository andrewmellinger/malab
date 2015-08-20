package com.crashbox.drudgemod.ai;

import com.crashbox.drudgemod.messaging.Message;
import net.minecraft.item.Item;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class MessageItemRequest extends Message
{
    public MessageItemRequest(EntityAIDrudge drudge, Item itemType, int quantity)
    {
        super(drudge);
        _drudgeAI = drudge;
        _itemType = itemType;
        _quantity = quantity;
    }

    public Item getItemType()
    {
        return _itemType;
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
                ", _itemType=" + _itemType +
                ", _quantity=" + _quantity +
                '}';
    }

    private final EntityAIDrudge _drudgeAI;
    private final Item _itemType;
    private final int _quantity;
}
