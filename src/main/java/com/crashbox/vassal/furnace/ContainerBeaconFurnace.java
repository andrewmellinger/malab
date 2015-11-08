package com.crashbox.vassal.furnace;

import com.crashbox.vassal.common.SampleFuelSlot;
import com.crashbox.vassal.common.SampleSlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class ContainerBeaconFurnace extends Container
{
    private final TileEntityBeaconFurnace _tileBeacon;
    private final int _sizeInventory;
    private int[] _trackedFields = {0, 0, 0, 0};


    public ContainerBeaconFurnace(InventoryPlayer inventoryPlayer, TileEntityBeaconFurnace tileEntity)
    {
        // DEBUG
        //LOGGER.debug("Constructed!!");

        _tileBeacon = tileEntity;
        _sizeInventory = _tileBeacon.getSizeInventory();

        // Set up all our main interaction slots
        addSlotToContainer(new Slot(_tileBeacon, 0, 88, 17));
        addSlotToContainer(new SlotFurnaceFuel(_tileBeacon, 1, 88, 53));
        addSlotToContainer(new SlotFurnaceOutput(inventoryPlayer.player, _tileBeacon, 2, 148, 35));

        for (int i = 0; i < 4; ++i)
            addSlotToContainer(new SampleSlot(tileEntity, i + 3, 8 + i * 18, 17));

        for (int i = 0; i < 4; ++i)
            addSlotToContainer(new SampleFuelSlot(tileEntity, i + 7, 8 + i * 18, 53));

        // add player inventory slots
        // note that the slot numbers are within the player inventory so can
        // be same as the tile entity inventory
        int i;
        for (i = 0; i < 3; ++i)
        {
            for (int j = 0; j < 9; ++j)
            {
                addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
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

//        LOGGER.debug("detectAndSendChanges!!");
//        VassalUtils.showStack(LOGGER);

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
        // NOTE: Merge returns true if it can merge any of it.  It returns false if it can't.
        // If it didn't use it all, we return ENTIRE THING WE TRIED TO MOVE!?!?

        ItemStack itemStack = null;
        Slot slot = (Slot) inventorySlots.get(slotIndex);

        int inIndex = TileEntityBeaconFurnace.slotEnum.INPUT_SLOT.ordinal();     // 0
        int fuelIndex = TileEntityBeaconFurnace.slotEnum.FUEL_SLOT.ordinal();   // 1
        int outIndex = TileEntityBeaconFurnace.slotEnum.OUTPUT_SLOT.ordinal();   // 2

        if (slot != null && slot.getHasStack())
        {
            ItemStack itemStack1 = slot.getStack();
            itemStack = itemStack1.copy();

            if (slotIndex == outIndex)
            {
                // FROM Output
                if (!mergeItemStack(itemStack1, _sizeInventory, _sizeInventory + 36, true))
                {
                    return null;
                }

                // Basically this says, try to make me more.
                slot.onSlotChange(itemStack1, itemStack);
            }
            else if (slotIndex < _sizeInventory)
            {
                // FROM samples, input, fuel
                if (!mergeItemStack(itemStack1, _sizeInventory, _sizeInventory + 36, true))
                {
                    return null;
                }
            }
            else if (FurnaceRecipes.instance().getSmeltingResult(itemStack1) != null)
            {
                // To INPUT
                if (!mergeIfMatches(itemStack1, slotIndex, inIndex))
                    return null;
            }
            else if (TileEntityFurnace.isItemFuel(itemStack1))
            {
                // To FUEL
                if (!mergeIfMatches(itemStack1, slotIndex, fuelIndex))
                    return null;
            }
            else
            {
                // Player inventory movement
                if (!mergeToOtherPlayerInventoryPart(itemStack1, slotIndex))
                    return null;
            }

            // If all was used up, clear out the slot.
            if (itemStack1.stackSize == 0)
                slot.putStack(null);
            else
                slot.onSlotChanged();

            // We didn't change anything
            if (itemStack1.stackSize == itemStack.stackSize)
                return null;

            slot.onPickupFromSlot(playerIn, itemStack1);
        }

        return itemStack;
    }

    private boolean mergeIfMatches(ItemStack itemStack1, int slotIndex, int targetIndex)
    {
        // TO fuel
        ItemStack targetStack = ((Slot) inventorySlots.get(targetIndex)).getStack();
        // If fuel already has stuff, just put into normal inventory
        if (targetStack == null || targetStack.isItemEqual(itemStack1))
        {
            return this.mergeItemStack(itemStack1, targetIndex, targetIndex + 1, false);
        }
        else
        {
            // Fallback to player inventory swapping
            return mergeToOtherPlayerInventoryPart(itemStack1, slotIndex);
        }

    }

    private boolean mergeToOtherPlayerInventoryPart(ItemStack itemstack, int slotIndex)
    {
        if (slotIndex >= _sizeInventory && slotIndex < _sizeInventory + 27)
        {
            // Inventory goes to hotbar
            return mergeItemStack(itemstack, _sizeInventory + 27, _sizeInventory + 36, false);
        }
        else if (slotIndex >= _sizeInventory + 27 && slotIndex < _sizeInventory + 36)
        {
            // Things to inventory
            return mergeItemStack(itemstack, _sizeInventory, _sizeInventory + 27, false);
        }

        return false;
    }
}
