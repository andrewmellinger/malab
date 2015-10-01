package com.crashbox.vassal.entity;

import net.minecraft.inventory.InventoryBasic;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class InventoryEntityVassal extends InventoryBasic
{
    public InventoryEntityVassal(EntityVassal vassal)
    {
        super("Label", false, 2);
        _vassal = vassal;
        setInventorySlotContents(0, vassal.getHeldItem());
        setInventorySlotContents(1, vassal.getFuelStack());
    }


    public void flushItemsToVassal()
    {
        _vassal.setCurrentItemOrArmor(0, getStackInSlot(0));
        _vassal.setFuelStack(getStackInSlot(1));
    }

//    @Override
//    public ItemStack getStackInSlotOnClosing(int slot)
//    {
//        if (slot == 0)
//        {
//            LOGGER.debug(getStackInSlot(0));
//            LOGGER.debug(_vassal.getHeldItem());
//        }
//        return super.getStackInSlotOnClosing(slot);
//    }

    private final EntityVassal _vassal;
    private static final Logger LOGGER = LogManager.getLogger();
}
