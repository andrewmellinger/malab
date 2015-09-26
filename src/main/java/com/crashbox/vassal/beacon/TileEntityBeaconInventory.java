package com.crashbox.vassal.beacon;

import com.crashbox.vassal.VassalUtils;
import com.crashbox.vassal.common.ItemStackMatcher;
import com.crashbox.vassal.messaging.IMessager;
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
    /**
     * Puts the contents of the stack into the inventory.
     * @param stack The contents to put in.
     * @return The remainder or null.
     */
    public ItemStack mergeIntoBestSlot(ItemStack stack)
    {
        // First try to fill loaded slots, then go back
        // and put rest into empty.
        //LOGGER.debug("mergeIntoBestSlot: stack=" + stack);

        int firstEmpty = -1;
        for (int i : getInputSlots())
        {
            //LOGGER.debug("-> checking slot:" + i);
            if (isItemValidForSlot(i, stack))
            {
                //LOGGER.debug("---> item valid");
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
                    VassalUtils.mergeStacks(current, stack);
                    if (stack.stackSize == 0)
                    {
                        return null;
                    }
                }
            }
//            else
//            {
//                LOGGER.debug("---> item NOT valid");
//            }
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
            //LOGGER.debug("extractItems: slot=" + i + ", stack=" + stack);
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

    public abstract int[] getOutputSlots();

    public abstract int[] getInputSlots();

    private static final Logger LOGGER = LogManager.getLogger();
}


