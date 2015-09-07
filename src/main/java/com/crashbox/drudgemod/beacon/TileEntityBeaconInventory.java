package com.crashbox.drudgemod.beacon;

import com.crashbox.drudgemod.messaging.IMessager;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.tileentity.TileEntityLockable;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public abstract class TileEntityBeaconInventory extends TileEntityLockable implements IUpdatePlayerListBox, IInventory,
        IMessager
{
    public ItemStack mergeIntoSlot(ItemStack stack, int slot)
    {
        ItemStack current = getStackInSlot(slot);

        // We might have been told to put things into a slot that might have become empty
        if (current == null)
        {
            setInventorySlotContents(slot, stack);
        }
        else if (current.isItemEqual(stack))
        {
            int freeSpace = current.getMaxStackSize() - current.stackSize;
            if (freeSpace > stack.stackSize)
            {
                current.stackSize += stack.stackSize;
                stack = null;
            }
            else
            {
                // Not enough room
                current.stackSize += freeSpace;
                stack.stackSize -= freeSpace;
            }
        }

        return stack;
    }
}
