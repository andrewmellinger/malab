package com.crashbox.mal.quarry;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class ContainerBeaconQuarry extends Container
{
    @Override
    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return true;
    }
}
