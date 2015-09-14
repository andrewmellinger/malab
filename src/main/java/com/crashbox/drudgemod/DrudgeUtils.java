package com.crashbox.drudgemod;

import com.crashbox.drudgemod.common.ItemStackMatcher;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

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

    public static boolean willDrop(World world, BlockPos pos, ItemStackMatcher matcher)
    {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (block.isAir(world, pos))
        {
            return false;
        }

        for (ItemStack stack : block.getDrops(world, pos, state, 0))
        {
            if (matcher.matches(stack))
                return true;
        }

        return false;
    }

    public static ItemStack identifyWillDrop(World world, BlockPos pos, ItemStackMatcher matcher)
    {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (block.isAir(world, pos))
        {
            return null;
        }

        for (ItemStack stack : block.getDrops(world, pos, state, 0))
        {
            if (matcher.matches(stack))
            {
                ItemStack tmp = stack.copy();
                tmp.stackSize = 0;
                return tmp;
            }
        }

        return null;
    }


    public static boolean isNotNull(Object obj, Logger logger)
    {
        if (obj == null)
        {
            logger.debug("Object is null and not expected: " + obj);
            return false;
        }
        return true;

    }

    public static String getSimpleName(Object o)
    {
        if (o == null)
            return null;

        return o.getClass().getSimpleName();
    }


//    public static boolean willDrop(World world, BlockPos pos, ItemStack sample)
//    {
//        IBlockState state = world.getBlockState(pos);
//        Block block = state.getBlock();
//
//        if (block.isAir(world, pos))
//        {
//            return false;
//        }
//
//        for (ItemStack stack : block.getDrops(world, pos, state, 0))
//        {
//            if (stack.isItemEqual(sample))
//                return true;
//        }
//
//        return false;
//    }

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

    public static boolean isWithinSqDist(BlockPos pos1, BlockPos pos2, int diff)
    {
        // We only use XZ.
        int xOffset = pos1.getX() - pos2.getX();
        int zOffset = pos1.getZ() - pos2.getZ();
        int dist = (xOffset * xOffset) + (zOffset * zOffset);
        return (dist <= diff);
    }

    public static int sqDistXZ(BlockPos pos1, BlockPos pos2)
    {
        // We only use XZ.
        int xOffset = pos1.getX() - pos2.getX();
        int zOffset = pos1.getZ() - pos2.getZ();
        return (xOffset * xOffset) + (zOffset * zOffset);
    }


    public static boolean pointInArea(BlockPos point, BlockPos center, int radius)
    {
        return !( point.getX() < center.getX() - radius || point.getX() > center.getX() + radius ||
                 point.getZ() < center.getZ() - radius || point.getZ() > center.getZ() + radius );
    }

    public static boolean pointInAreas(BlockPos point, List<BlockPos> centers, int radius)
    {
        for (BlockPos pos : centers)
        {
            if (pointInArea(point, pos, radius))
                return true;
        }
        return false;
    }


    public static String objID(Object obj)
    {
        if (obj == null)
            return "null";

        return obj.getClass().getSimpleName() + "@" + Integer.toHexString(obj.hashCode());
    }


    private static final Logger LOGGER = LogManager.getLogger();
}
