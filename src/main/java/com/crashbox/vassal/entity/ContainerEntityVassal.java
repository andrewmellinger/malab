package com.crashbox.vassal.entity;

import com.crashbox.vassal.common.ItemStackMatcher;
import com.crashbox.vassal.common.SampleMatcherSlot;
import com.crashbox.vassal.common.SampleSlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class ContainerEntityVassal extends Container
{
    public ContainerEntityVassal(InventoryPlayer inventoryPlayer,
            EntityVassal vassal, InventoryEntityVassal inventory)
    {
        _vassal = vassal;
        _inventory = inventory;

        // Add slots
        addSlotToContainer(new Slot(_inventory, 0, 15, 17));
        addSlotToContainer(new Slot(_inventory, 1, 15, 53));

        // Follow me slot
        addSlotToContainer(new SampleMatcherSlot(_inventory, 2, 143, 17,
                new ItemStackMatcher(Items.redstone)));

        // add player inventory slots
        // note that the slot numbers are within the player inventory so can
        // be same as the tile entity inventory
        int i;
        for (i = 0; i < 3; ++i)
        {
            for (int j = 0; j < 9; ++j)
            {
                addSlotToContainer(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 84+i*18));
            }
        }

        // add hotbar slots
        for (i = 0; i < 9; ++i)
        {
            // TODO:  Is this really the right slot number??
            addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 142));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return true;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index)
    {
        ItemStack itemstack = null;
        Slot slot = (Slot) this.inventorySlots.get(index);

        // DESIGN:
        // Held slot: 0
        // Fuel slot: 1
        // Follow me: 2

        // Player Inv: 3 - 29
        // Player hot bar: 30-38

        if (slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (index == 0 || index == 1 || index == 2)
            {
                // Get stuff out of bot fields
                if (!mergeItemStack(itemstack1, 3, 38, true))
                    return null;
            }
            else
            {
                // We only support putting things into fuel.
                // Otherwise we really want them to specify, because not all are the same.
                ItemStack current = getSlot(1).getStack();
                if (TileEntityFurnace.isItemFuel(itemstack1) &&
                        (current == null || current.isItemEqual(itemstack1)))
                {
                    if (!mergeItemStack(itemstack1, 1, 2, false))
                        return null;
                }
            }

            if (itemstack1.stackSize == 0)
                slot.putStack((ItemStack)null);
            else
                slot.onSlotChanged();

            if (itemstack1.stackSize == itemstack.stackSize)
                return null;

            slot.onPickupFromSlot(playerIn, itemstack1);
        }

        return itemstack;
    }

    @Override
    public void onContainerClosed(EntityPlayer playerIn)
    {
        // Put the thing into the vassal
        super.onContainerClosed(playerIn);
        _inventory.flushItemsToVassal();
        if (_inventory.getStackInSlot(2) != null)
        {
            _vassal.setFollowPlayer(playerIn);
        }

        _vassal.resume();
    }

    private final EntityVassal _vassal;
    private final InventoryEntityVassal _inventory;
    private static final Logger LOGGER = LogManager.getLogger();
}
