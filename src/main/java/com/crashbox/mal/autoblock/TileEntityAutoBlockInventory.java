package com.crashbox.mal.autoblock;

import com.crashbox.mal.util.MALUtils;
import com.crashbox.mal.common.ItemStackMatcher;
import com.crashbox.mal.messaging.IMessager;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.tileentity.TileEntityLockable;
import net.minecraft.util.BlockPos;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public abstract class TileEntityAutoBlockInventory extends TileEntityLockable implements IUpdatePlayerListBox, IInventory,
        IMessager
{
    /**
     * Puts the contents of the stack into the inventory.  Similar to the container
     * "mergeIntoSlot" api, but is used for things that don't get a container
     * such as directly to the tile entity.
     * @param stack The contents to put in.
     * @return The remainder or null.
     */
    public ItemStack mergeIntoBestSlot(ItemStack stack)
    {
        // First try to fill loaded slots, then go back
        // and put rest into empty.
        int firstEmpty = -1;
        for (int i : getInputSlots())
        {
            if (isItemValidForSlot(i, stack))
            {
                ItemStack current = getStackInSlot(i);
                if ( current == null)
                {
                    if (firstEmpty == -1)
                    {
                        firstEmpty = i;
                    }
                }
                else
                {
                    MALUtils.mergeStacks(current, stack);
                    if (stack.stackSize == 0)
                    {
                        return null;
                    }
                }
            }
        }

        if (firstEmpty != -1)
        {
            setInventorySlotContents(firstEmpty, stack.copy());
            return null;
        }

        return stack;
    }

    public ItemStack extractItems(ItemStackMatcher matcher, int wanted)
    {
        ItemStack returnStack = null;
        for (int i : getOutputSlots())
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

    @Override
    public BlockPos getBlockPos()
    {
        return getPos();
    }

    /**
     * @return Indexes of all available output slots.
     */
    public abstract int[] getOutputSlots();

    /**
     * @return Indexes of all available input slots.
     */
    public abstract int[] getInputSlots();

}


