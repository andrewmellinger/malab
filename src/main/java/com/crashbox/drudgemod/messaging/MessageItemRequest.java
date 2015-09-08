package com.crashbox.drudgemod.messaging;

import com.crashbox.drudgemod.common.ItemStackMatcher;
import net.minecraft.item.ItemStack;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class MessageItemRequest extends  Message
{
    public MessageItemRequest(IMessager sender, IMessager receiver, Object cause, ItemStackMatcher matcher, int quantity)
    {
        super(sender, receiver, cause, 0);
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

    public void debugInfo(StringBuilder builder)
    {
        super.debugInfo(builder);
        builder.append(", _matcher=").append(_matcher);
        builder.append(", _quantity=").append(_quantity);
    }

    private final ItemStackMatcher _matcher;
    private final int _quantity;
}
