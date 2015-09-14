package com.crashbox.drudgemod.beacon;

import com.crashbox.drudgemod.DrudgeUtils;
import com.crashbox.drudgemod.common.ItemStackMatcher;
import com.crashbox.drudgemod.messaging.IMessager;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.tileentity.TileEntityLockable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Copyright 2015 Andrew O. Mellinger
 */
public abstract class TileEntityBeaconInventory extends TileEntityLockable implements IUpdatePlayerListBox, IInventory,
        IMessager
{
    public ItemStack mergeIntoSlot(ItemStack stack, int slot)
    {
        LOGGER.debug("mergeIntoSlot: " + slot);
        if (slot == -1)
        {
            stack = mergeIntoBestSlot(stack);
            LOGGER.debug("mergeIntoBestSlot returned: " + stack);
            return stack;
        }

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

    public ItemStack mergeIntoBestSlot(ItemStack stack)
    {
        // Find similar one
        for (int i = 0; i < getSizeInventory(); ++i)
        {
            ItemStack current = getStackInSlot(i);
            if (current != null && current.isItemEqual(stack))
            {
                DrudgeUtils.mergeStacks(current, stack);
                if (stack.stackSize == 0)
                    return null;
            }
        }

        // If we are here we have some left.   Put in an empty one
        for (int i = 0; i < getSizeInventory(); ++i)
        {
            ItemStack current = getStackInSlot(i);
            if (current == null)
            {
                // This uses the rest
                setInventorySlotContents(i, stack.copy());
                stack.stackSize = 0;
                return null;
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

    private static final Logger LOGGER = LogManager.getLogger();
}


