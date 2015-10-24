package com.crashbox.vassal.entity;

import net.minecraft.inventory.InventoryBasic;
import net.minecraft.util.ChatComponentTranslation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class InventoryEntityVassal extends InventoryBasic
{
    public InventoryEntityVassal(EntityVassal vassal)
    {

        super(new ChatComponentTranslation("container.vassal.title").getUnformattedText(), false, 3);
        _vassal = vassal;
        setInventorySlotContents(0, vassal.getHeldItem());
        setInventorySlotContents(1, vassal.getFuelStack());
        setInventorySlotContents(2, vassal.getFollowMeStack());
    }

    public void flushItemsToVassal()
    {
        _vassal.setCurrentItemOrArmor(0, getStackInSlot(0));
        _vassal.setFuelStack(getStackInSlot(1));
        _vassal.setFollowMeStack(getStackInSlot(2));
    }

    private final EntityVassal _vassal;
    private static final Logger LOGGER = LogManager.getLogger();
}
