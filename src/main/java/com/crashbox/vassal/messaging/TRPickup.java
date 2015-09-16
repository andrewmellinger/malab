package com.crashbox.vassal.messaging;

import com.crashbox.vassal.common.ItemTypeMatcher;
import com.crashbox.vassal.task.TaskPickup;
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
