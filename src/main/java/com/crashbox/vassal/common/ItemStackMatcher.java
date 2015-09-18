package com.crashbox.vassal.common;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class ItemStackMatcher
{
    public ItemStackMatcher(ItemStack... samples)
    {
        for (ItemStack sample : samples)
        {
            add(sample);
        }
    }

    public void add(ItemStack sample)
    {
        if (sample != null)
        {
            ItemStack tmp = sample.copy();
            tmp.stackSize = 0;
            _samples.add(tmp);
        }
    }

    public boolean matches(ItemStack stack)
    {
        for (ItemStack sample : _samples)
        {
            if (sample.isItemEqual(stack))
                return true;
        }
        return false;
    }

    public boolean matches(Item item)
    {
        for (ItemStack sample : _samples)
        {
            if (sample.getItem().equals(item))
                return true;
        }
        return false;
    }




    public int size()
    {
        return _samples.size();
    }

    @Override
    public String toString()
    {
        return "ItemStackMatcher{" +
                "_samples=" + _samples +
                '}';
    }


    private final List<ItemStack> _samples = new ArrayList<ItemStack>();
}
