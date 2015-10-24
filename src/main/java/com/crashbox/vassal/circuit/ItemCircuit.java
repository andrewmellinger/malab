package com.crashbox.vassal.circuit;

import com.crashbox.vassal.VassalMain;
import com.crashbox.vassal.grenades.EntityDiggerGrenade;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class ItemCircuit extends Item
{
    public static String NAME = "circuit";

    public ItemCircuit()
    {
        setMaxStackSize(64);
        setCreativeTab(VassalMain.VASSAL_TAB);

        setUnlocalizedName(NAME);
    }
}
