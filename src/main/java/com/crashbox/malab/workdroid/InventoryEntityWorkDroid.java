package com.crashbox.malab.workdroid;

import net.minecraft.inventory.InventoryBasic;
import net.minecraft.util.ChatComponentTranslation;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class InventoryEntityWorkDroid extends InventoryBasic
{
    public InventoryEntityWorkDroid(EntityWorkDroid workDroid)
    {
        super(new ChatComponentTranslation("container.malab.workDroid.title").getUnformattedText(), false, 3);
        _workDroid = workDroid;
        setInventorySlotContents(0, workDroid.getHeldItem());
        setInventorySlotContents(1, workDroid.getFuelStack());
        setInventorySlotContents(2, workDroid.getFollowMeStack());
    }

    public void flushItemsToWorkDroids()
    {
        _workDroid.setCurrentItemOrArmor(0, getStackInSlot(0));
        _workDroid.setFuelStack(getStackInSlot(1));
        _workDroid.setFollowMeStack(getStackInSlot(2));
    }

    private final EntityWorkDroid _workDroid;
}
