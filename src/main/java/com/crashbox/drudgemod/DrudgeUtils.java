package com.crashbox.drudgemod;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class DrudgeUtils
{
    public static void showStack()
    {
        try
        {
            throw new Exception("Arg");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static boolean willDrop(World world, BlockPos pos, ItemStack sample)
    {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (block.isAir(world, pos))
        {
            return false;
        }

        for (ItemStack stack : block.getDrops(world, pos, state, 0))
        {
            if (stack.isItemEqual(sample))
                return true;
        }

        return false;

    }

    public static boolean harvestInto(World world, BlockPos pos, ItemStack stack)
    {
        // TODO:  Check all tool pre-requisites

        if (stack.stackSize >= stack.getMaxStackSize())
            return false;

        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();



        if (Block.getBlockFromItem(stack.getItem()) == block)
        {
            int meta = block.getMetaFromState(state);

            // Strip log AXIS data
            if (block == Blocks.log || block == Blocks.log2)
            {
                meta = meta & 3;
            }

            if (meta == stack.getMetadata())
            {
                world.destroyBlock(pos, false);
                stack.stackSize += 1;
                return true;
            }
        }

        return false;
    }


    private static final Logger LOGGER = LogManager.getLogger();
}
