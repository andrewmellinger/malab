package com.crashbox.malab.chest;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class ContainerAutoChest extends Container
{
    private final IInventory _tileInventory;
    private final int _sizeInventory;
    private int[] _trackedFields = { 0,0,0,0};

    public ContainerAutoChest(InventoryPlayer inventoryPlayer, IInventory inventory)
    {
        // DEBUG
        _tileInventory = inventory;
        _sizeInventory = _tileInventory.getSizeInventory();

        // Add our inventory
        for (int i = 0; i < 3; ++i)
        {
            for (int j = 0; j < 9; ++j)
            {
                addSlotToContainer(new Slot(_tileInventory, j+i*9, 8+j*18, 15+i*18));
            }
        }

        // add player inventory slots
        // note that the slot numbers are within the player inventory so can
        // be same as the tile entity inventory
        for (int i = 0; i < 3; ++i)
        {
            for (int j = 0; j < 9; ++j)
            {
                addSlotToContainer(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 84+i*18));
            }
        }

        // add hotbar slots
        for (int i = 0; i < 9; ++i)
        {
            addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 142));
        }
    }

    @Override
    public void addCraftingToCrafters(ICrafting listener)
    {
        super.addCraftingToCrafters(listener);
        listener.func_175173_a(this, _tileInventory);
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
                int tmp = _tileInventory.getField(n);
                if (_trackedFields[n] != tmp)
                {
                    icrafting.sendProgressBarUpdate(this, n, tmp);
                }
            }
        }

        // cache state for next time
        for (int n = 0; n < _trackedFields.length; ++n)
        {
            _trackedFields[n] = _tileInventory.getField(n);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int data)
    {
        _tileInventory.setField(id, data);
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return _tileInventory.isUseableByPlayer(playerIn);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int slotIndex)
    {
        ItemStack itemStack = null;
        Slot slot = (Slot)inventorySlots.get(slotIndex);

        if (slot != null && slot.getHasStack())
        {
            ItemStack itemStack1 = slot.getStack();
            itemStack = itemStack1.copy();

            if (slotIndex < _sizeInventory)
            {
                if (!this.mergeItemStack(itemStack1, _sizeInventory, this.inventorySlots.size(), true))
                {
                    return null;
                }
            }
            else if (!this.mergeItemStack(itemStack1, 0, _sizeInventory, false))
            {
                return null;
            }

            if (itemStack1.stackSize == 0)
                slot.putStack(null);
            else
                slot.onSlotChanged();

            if (itemStack1.stackSize == itemStack.stackSize)
                return null;

            slot.onPickupFromSlot(playerIn, itemStack1);
        }

        return itemStack;
    }
}
