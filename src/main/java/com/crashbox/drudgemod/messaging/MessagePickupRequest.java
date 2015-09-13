package com.crashbox.drudgemod.messaging;

import com.crashbox.drudgemod.common.ItemStackMatcher;
import com.crashbox.drudgemod.task.TaskBase;
import com.crashbox.drudgemod.task.TaskPickup;
import net.minecraft.item.Item;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class MessagePickupRequest extends MessageAcquireRequest
{
    public MessagePickupRequest(IMessager sender, IMessager target, Object transactionID, int priority,
            int quantity, Item item)
    {
        super(sender, target, transactionID, priority, TaskPickup.class, null, quantity);
        _item = item;
    }

    public Item getItem()
    {
        return _item;
    }

    public void debugInfo(StringBuilder builder)
    {
        super.debugInfo(builder);
        builder.append(", _item=").append(_item);
    }

    private final Item _item;
}
