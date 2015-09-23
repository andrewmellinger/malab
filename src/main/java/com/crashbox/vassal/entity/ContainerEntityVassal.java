package com.crashbox.vassal.entity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
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
        addSlotToContainer(new Slot(_inventory, 0, 8, 15));

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
    public void onContainerClosed(EntityPlayer playerIn)
    {
        // Put the thing into the vassal
        super.onContainerClosed(playerIn);
        _inventory.flushItemsToVassal();
        _vassal.resume();
    }

    private final EntityVassal _vassal;
    private final InventoryEntityVassal _inventory;
    private static final Logger LOGGER = LogManager.getLogger();
}