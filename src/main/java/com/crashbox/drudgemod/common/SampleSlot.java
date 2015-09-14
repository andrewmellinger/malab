package com.crashbox.drudgemod.common;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class SampleSlot extends Slot
{

    public SampleSlot(IInventory inventoryIn, int index, int xPosition, int yPosition)
    {
        super(inventoryIn, index, xPosition, yPosition);
    }

    @Override
    public boolean isItemValid(ItemStack stack)
    {
        return true;
    }

    @Override
    public int getItemStackLimit(ItemStack stack)
    {
        return 1;
    }

    private static final Logger LOGGER = LogManager.getLogger();
}
