package com.crashbox.drudgemod.messaging;

import net.minecraft.item.ItemStack;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class MessageStorageRequest extends Message
{
    // Used to request a place to store this item
    public MessageStorageRequest(IMessager sender, IMessager target, Object transactionID, int priority,
            ItemStack stack)
    {
        super(sender, target, transactionID, priority);
        _stack = stack;
    }

    public ItemStack getStack()
    {
        return _stack;
    }

    private final ItemStack _stack;
}
