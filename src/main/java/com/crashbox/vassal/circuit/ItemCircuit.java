package com.crashbox.vassal.circuit;

import com.crashbox.vassal.VassalMain;
import net.minecraft.item.Item;

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
