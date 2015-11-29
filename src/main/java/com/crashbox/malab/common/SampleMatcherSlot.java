package com.crashbox.malab.common;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class SampleMatcherSlot extends SampleSlot
{
    public SampleMatcherSlot(IInventory inventoryIn, int index, int xPosition, int yPosition,
                             ItemStackMatcher matcher)
    {
        super(inventoryIn, index, xPosition, yPosition);
        _matcher = matcher;
    }

    @Override
    public boolean isItemValid(ItemStack stack)
    {
        return _matcher.matches(stack);
    }

    private final ItemStackMatcher _matcher;
}
