package com.crashbox.drudgemod.common;

import net.minecraft.item.ItemStack;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class AnyItemMatcher extends ItemStackMatcher
{
    @Override
    public boolean matches(ItemStack stack)
    {
        return true;
    }
}
