package com.crashbox.mal.circuit;

import com.crashbox.mal.MALMain;
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
        setCreativeTab(MALMain.MAL_TAB);

        setUnlocalizedName(NAME);
    }
}
