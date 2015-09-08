package com.crashbox.drudgemod.messaging;

import com.crashbox.drudgemod.common.ItemStackMatcher;
import com.crashbox.drudgemod.task.TaskDeliver;
import net.minecraft.item.ItemStack;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class MessageDeliverRequest extends MessageTaskRequest
{
    public MessageDeliverRequest(IMessager sender, IMessager receiver, Object cause, int priority,
            ItemStackMatcher matcher, int quantity, int slot)
    {
        super(sender, receiver, cause, priority, TaskDeliver.class);
        _matcher = matcher;
        _quantity = quantity;
        _slot = slot;
    }

    public ItemStackMatcher getMatcher()
    {
        return _matcher;
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
        builder.append(", itemSample=").append(_matcher);
        builder.append(", quantity=").append(_quantity);
        builder.append(", slot=").append(_slot);
    }

    private final ItemStackMatcher _matcher;
    private final int _quantity;
    private final int _slot;
}
