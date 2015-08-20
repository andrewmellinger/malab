package com.crashbox.drudgemod.tasker;

import com.crashbox.drudgemod.messaging.IMessageSender;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.tileentity.TileEntityLockable;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public abstract class TileEntityTaskerInventory extends TileEntityLockable implements IUpdatePlayerListBox, IInventory,
        IMessageSender
{
    public ItemStack mergeIntoSlot(ItemStack stack, int slot)
    {
        ItemStack current = getStackInSlot(slot);

        if (current.isItemEqual(stack))
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
