package com.crashbox.vassal.common;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class ItemStackMatcher
{
    public static ItemStackMatcher getQuarryMatcher()
    {
        if (QUARRY_MATCHER != null)
            return QUARRY_MATCHER;

        //  TODO:  Load from config?
        ItemStackMatcher matcher = new ItemStackMatcher();
        matcher.add(new ItemStack(Item.getItemFromBlock(Blocks.cobblestone)));
        //matcher.add(new ItemStack(Item.getItemFromBlock(Blocks.gravel)));
        matcher.add(Items.flint);
        matcher.add(Items.coal);

        // TODO:  Add granite

        QUARRY_MATCHER = matcher;

        return QUARRY_MATCHER;
    }

    // ===================

    public ItemStackMatcher()
    {
    }

    public ItemStackMatcher(ItemStack... samples)
    {
        for (ItemStack sample : samples)
        {
            add(sample);
        }
    }

    public ItemStackMatcher(Block... blocks)
    {
        for (Block block : blocks)
        {
            add(block);
        }
    }

    public ItemStackMatcher(Item... items)
    {
        for (Item item : items)
        {
            add(item);
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

    public void add(Block sample)
    {
        if (sample != null)
        {
            _samples.add(new ItemStack(sample, 0));
        }
    }

    public void add(Item sample)
    {
        if (sample != null)
        {
            _samples.add(new ItemStack(sample, 0));
        }
    }

    public boolean matches(ItemStack stack)
    {
        if (stack == null)
            return false;

        for (ItemStack sample : _samples)
        {
            if (sample.isItemEqual(stack))
                return true;
        }
        return false;
    }

    /**
     * Matches IGNORING metadata
     * @param item The plain item
     * @return True if the item is in one of the stacks ignoring metadata
     */
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


    // ===========
    private static ItemStackMatcher QUARRY_MATCHER;

}
