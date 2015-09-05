package com.crashbox.drudgemod.messaging;

import com.crashbox.drudgemod.ai.EntityAIDrudge;
import com.crashbox.drudgemod.messaging.Message;
import com.crashbox.drudgemod.task.TaskDeliver;
import net.minecraft.item.ItemStack;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class MessageDeliverRequest extends MessageItemRequest
{
    public MessageDeliverRequest(IMessager sender, IMessager receiver, int priority,
            ItemStack itemSample, int quantity, int slot)
    {
        super(sender, receiver, TaskDeliver.class, priority, itemSample, quantity);
        _slot = slot;
    }

    public int getSlot()
    {
        return _slot;
    }

    private final int _slot;
}
