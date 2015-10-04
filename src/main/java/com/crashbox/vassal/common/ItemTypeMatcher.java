package com.crashbox.vassal.common;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Copyright 2015 Andrew O. Mellinger
 *
 * This matcher really only cares about specific item types and ignores metadata.
 */
public class ItemTypeMatcher extends ItemStackMatcher
{
    public ItemTypeMatcher(Item item)
    {
        _item = item;
    }

    @Override
    public boolean matches(ItemStack stack)
    {
        if (stack == null)
            return false;

        return stack.getItem().equals(_item);
    }

    private final Item _item;
}
