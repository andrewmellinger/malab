package com.crashbox.mal.common;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class NotItemStackMatcher extends ItemStackMatcher
{
    public NotItemStackMatcher()
    {
    }

    public NotItemStackMatcher(ItemStack... samples)
    {
        super(samples);
    }

    public NotItemStackMatcher(Block... blocks)
    {
        super(blocks);
    }

    public NotItemStackMatcher(Item... items)
    {
        super(items);
    }

    @Override
    public boolean matches(ItemStack stack)
    {
        return !super.matches(stack);
    }

    @Override
    public boolean matches(Item item)
    {
        return !super.matches(item);
    }
}
