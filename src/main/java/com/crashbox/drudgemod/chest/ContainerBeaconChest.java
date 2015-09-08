package com.crashbox.drudgemod.chest;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class ContainerBeaconChest extends Container
{
    private final IInventory _tileBeacon;
    private final int _sizeInventory;
    private int[] _trackedFields = { 0,0,0,0};

    public ContainerBeaconChest(InventoryPlayer inventoryPlayer, IInventory inventory)
    {
        // DEBUG
        LOGGER.debug("Constructed!!");

        _tileBeacon = inventory;
        _sizeInventory = _tileBeacon.getSizeInventory();

        // Set up all our main interaction slots
        addSlotToContainer(new Slot(_tileBeacon, 0, 56, 17));
        addSlotToContainer(new SlotFurnaceFuel(_tileBeacon, 1, 56, 53));
        addSlotToContainer(new SlotFurnaceOutput(inventoryPlayer.player, _tileBeacon, 2, 116, 35));

        // TODO: Make reusable function

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
            addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 142));
        }
    }

    @Override
    public void addCraftingToCrafters(ICrafting listener)
    {
        super.addCraftingToCrafters(listener);
        listener.func_175173_a(this, _tileBeacon);
    }

    /**
     * Looks for changes made in the container, sends them to every listener.
     */
    @Override
    public void detectAndSendChanges()
    {
        super.detectAndSendChanges();

        // send updates to each crafter
        for (Object crafter : crafters)
        {
            ICrafting icrafting = (ICrafting) crafter;

            // send all fields to each crafter
            for (int n = 0; n < _trackedFields.length; ++n)
            {
                int tmp = _tileBeacon.getField(n);
                if (_trackedFields[n] != tmp)
                {
                    icrafting.sendProgressBarUpdate(this, n, tmp);
                }
            }
        }

        // cache state for next time
        for (int n = 0; n < _trackedFields.length; ++n)
        {
            _trackedFields[n] = _tileBeacon.getField(n);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int data)
    {
        _tileBeacon.setField(id, data);
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return _tileBeacon.isUseableByPlayer(playerIn);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int slotIndex)
    {
        // Look at chest

        ItemStack itemStack1 = null;
        Slot slot = (Slot)inventorySlots.get(slotIndex);

        if (slot != null && slot.getHasStack())
        {
            ItemStack itemStack2 = slot.getStack();
            itemStack1 = itemStack2.copy();

            if (!mergeItemStack(itemStack2, _sizeInventory, _sizeInventory + 36, false))
            {
                return null;
            }

            if (itemStack2.stackSize == 0)
                slot.putStack((ItemStack)null);
            else
                slot.onSlotChanged();

            if (itemStack2.stackSize == itemStack1.stackSize)
                return null;

            slot.onPickupFromSlot(playerIn, itemStack2);
        }

        return itemStack1;
    }

    private static final Logger LOGGER = LogManager.getLogger();

}