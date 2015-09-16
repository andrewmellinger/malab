package com.crashbox.vassal.common;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class SampleFuelSlot extends SampleSlot
{
    public SampleFuelSlot(IInventory inventoryIn, int index, int xPosition, int yPosition)
    {
        super(inventoryIn, index, xPosition, yPosition);
    }

    @Override
    public boolean isItemValid(ItemStack stack)
    {
        return TileEntityFurnace.getItemBurnTime(stack) > 0;
    }

}
