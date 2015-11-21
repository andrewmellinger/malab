package com.crashbox.mal.common;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Copyright 2015 Andrew O. Mellinger
 * <p/>
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
        return stack != null && stack.getItem().equals(_item);
    }

    private final Item _item;
}
