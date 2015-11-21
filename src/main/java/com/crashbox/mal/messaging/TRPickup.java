package com.crashbox.mal.messaging;

import com.crashbox.mal.common.ItemTypeMatcher;
import com.crashbox.mal.task.TaskPickup;
import net.minecraft.item.Item;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class TRPickup extends TRAcquireBase
{
    public TRPickup(IMessager sender, IMessager target, Object transactionID, int priority,
            int quantity, Item item)
    {
        super(sender, target, transactionID, priority, TaskPickup.class, new ItemTypeMatcher(item), quantity);
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
