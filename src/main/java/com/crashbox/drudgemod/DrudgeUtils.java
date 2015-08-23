package com.crashbox.drudgemod;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

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

    public static boolean sameType(World world, BlockPos pos, ItemStack stack)
    {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        return Block.getBlockFromItem(stack.getItem()) == block && block.getMetaFromState(state) == stack.getMetadata();
    }

    public static boolean canHarvestInto(World world, BlockPos pos, ItemStack stack)
    {
        if (stack.stackSize >= stack.getMaxStackSize())
            return false;

        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        return Block.getBlockFromItem(stack.getItem()) == block && block.getMetaFromState(state) == stack.getMetadata();
    }

    public static boolean harvestInto(World world, BlockPos pos, ItemStack stack)
    {
        // TODO:  Check all tool prerequisities

        if (stack.stackSize >= stack.getMaxStackSize())
            return false;

        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        // TODO Animate Breaking
        //block.getHarvestLevel(state);
        //block.harvestBlock();

        if (Block.getBlockFromItem(stack.getItem()) == block &&
            block.getMetaFromState(state) == stack.getMetadata())
        {
            world.destroyBlock(pos, false);
            stack.stackSize += 1;
        }

        return false;
    }

}
