package com.crashbox.malab;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class CreativeTabMALab extends CreativeTabs
{
    public CreativeTabMALab()
    {
        super(CreativeTabs.getNextID(), "mal");
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Item getTabIconItem()
    {
        return Item.getItemFromBlock(MALabMain.BLOCK_AUTO_QUARRY);
    }
}
