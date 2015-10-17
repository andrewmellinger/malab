package com.crashbox.vassal;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class CreativeTabVassal extends CreativeTabs
{
    public CreativeTabVassal()
    {
        super(CreativeTabs.getNextID(), "vassal");
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Item getTabIconItem()
    {
        return Item.getItemFromBlock(VassalMain.BLOCK_BEACON_QUARRY);
    }
}
