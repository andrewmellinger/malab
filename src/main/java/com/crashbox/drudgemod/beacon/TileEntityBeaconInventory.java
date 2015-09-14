package com.crashbox.drudgemod.beacon;

import com.crashbox.drudgemod.common.ItemStackMatcher;
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
            stack = null;
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

    public ItemStack extractItems(ItemStackMatcher matcher, int wanted)
    {
        ItemStack returnStack = null;
        for (int i = 0; i < getSizeInventory(); ++i)
        {
            ItemStack stack = getStackInSlot(i);
            if (matcher.matches(stack))
            {
                if (returnStack == null)
                {
                    returnStack = stack.copy();
                    returnStack.stackSize = 0;
                }

                // Figure out how we still want.
                int xfer = wanted - returnStack.stackSize;

                if (stack.stackSize > xfer)
                {
                    returnStack.stackSize += xfer;
                    stack.stackSize -= xfer;
                }
                else if (stack.stackSize <= xfer)
                {
                    returnStack.stackSize += stack.stackSize;
                    setInventorySlotContents(i, null);
                }

                if (returnStack.stackSize == wanted)
                    return returnStack;
            }
        }

        // We have run out of stuff.

        return returnStack;
    }

}
