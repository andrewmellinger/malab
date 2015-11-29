package com.crashbox.malab.circuit;

import com.crashbox.malab.MALabMain;
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
        setCreativeTab(MALabMain.MAL_TAB);

        setUnlocalizedName(NAME);
    }
}
