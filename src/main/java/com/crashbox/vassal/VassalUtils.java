package com.crashbox.vassal;

import com.crashbox.vassal.common.ItemStackMatcher;
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
public class VassalUtils
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

    /**
     * Puts the contents of the second on into the first one (target)
     * @param target The stack to add content too.
     * @param toAdd The stack to take content from.  May end up with zero size.
     */
    public static void mergeStacks(ItemStack target, ItemStack toAdd)
    {
        int xfer = target.getMaxStackSize() - target.stackSize;
        if (xfer > toAdd.stackSize)
            xfer = toAdd.stackSize;

        LOGGER.debug("XFER Size: " + xfer);
        target.stackSize += xfer;
        toAdd.stackSize -= xfer;
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

    /**
     * Finds the point on the radius edge where we'll intersect if we travel in a
     * straight line.
     * @param center Center of target area.
     * @param radius Radius of target area.
     * @param start Our starting (or current) location.
     * @return Most likely intersect.
     */
    public static BlockPos findIntersect(BlockPos center, int radius, BlockPos start)
    {
        // NOTE: Slope Z over X.
//        LOGGER.debug("Center: " + center);
//        LOGGER.debug("Start:  " + start);

        // Avoid divide by zero on the slope compute
        if ( start.getX() == center.getX())
        {
            return handleTopBottom(center, radius, start);
        }

//        LOGGER.debug("ZDiff: " + (start.getZ() - center.getZ()));
//        LOGGER.debug("XDiff: " + (start.getX() - center.getX()));

        // Make x diff abs, then sign tells us top or bottom
        double slope = (Math.abs(start.getZ() - center.getZ()) * 1.0F) /
                       (Math.abs(start.getX() - center.getX()) * 1.0F) ;

//        LOGGER.debug("Slope: " + slope);

        if (slope > 1)
            return handleTopBottom(center, radius, start);
        else
            return handleLeftRight(center, radius, start);
    }

    private static BlockPos handleLeftRight(BlockPos center, int radius, BlockPos start)
    {
        double frac = radius / Math.abs((start.getX() - center.getX()) * 1.0F);
        int z = center.getZ() + (int) (frac * (start.getZ() - center.getZ()));
        int x = 0;

        // Left or right
        if (start.getX() - center.getX() > 0)
        {
            // Right
            x = center.getX() + radius;
        }
        else
        {
            // Left
            x = center.getX() - radius;
        }
        return new BlockPos(x, center.getY(), z);
    }

    private static BlockPos handleTopBottom(BlockPos center, int radius, BlockPos start)
    {
        double frac = radius / Math.abs((start.getZ() - center.getZ()) * 1.0F);
        int z = 0;
        int x = center.getX() + (int) (frac * (start.getX() - center.getX()));

        if (start.getZ() - center.getZ() > 0)
        {
            // Bottom
            z = center.getZ() + radius;
        }
        else
        {
            // Top
            z = center.getZ() - radius;
        }
        return new BlockPos(x, center.getY(), z);
    }





    private static final Logger LOGGER = LogManager.getLogger();
}
