package com.crashbox.vassal;

import com.crashbox.vassal.ai.AIUtils;
import com.crashbox.vassal.common.ItemStackMatcher;
import com.crashbox.vassal.entity.EntityVassal;
import com.crashbox.vassal.util.BlockWalker;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStoneSlab;
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
    public static enum COMPASS { EAST, SOUTH, WEST, NORTH }

    public static void showStack()
    {
        try
        {
            throw new Exception("Arg");
        }
        catch (Exception e)
        {
            for (StackTraceElement elem : e.getStackTrace())
            {
                LOGGER.debug(elem);
            }
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

    // Return true if harvested
    public static boolean harvestBlock(World world, EntityVassal entity, BlockPos harvestBlock,
            ItemStackMatcher matcher)
    {
        ItemStack targetStack = entity.getHeldItem();
        if (targetStack == null)
        {
            targetStack = VassalUtils.identifyWillDrop(world, harvestBlock, matcher);
            if (targetStack == null)
                return false;
        }
        else
        {
            // It changed or won't drop the right thing, bail.
            if (!VassalUtils.willDrop(entity.getEntityWorld(), harvestBlock, new ItemStackMatcher(targetStack)))
                return false;
        }

        ///// BREAK
        world.destroyBlock(harvestBlock, true);

        ///// PICKUP
        AIUtils.collectEntityIntoStack(entity.getEntityWorld(), harvestBlock, 3, targetStack);

        if (entity.getHeldItem() == null && targetStack.stackSize > 0)
            entity.setCurrentItemOrArmor(0, targetStack);

        return true;
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

    //=============================================================================================

    public static BlockPos[] getCorners(BlockPos start, int radius)
    {
        return new BlockPos[] {
                new BlockPos(start.getX() - radius, start.getY(), start.getZ() - radius),
                new BlockPos(start.getX() + radius, start.getY(), start.getZ() - radius),
                new BlockPos(start.getX() + radius, start.getY(), start.getZ() + radius),
                new BlockPos(start.getX() - radius, start.getY(), start.getZ() + radius)
            };
    }

    //=============================================================================================

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


    //=============================================================================================
    // ####  #####  ####  #### ##### #   #  ####
    // #   #   #   #     #       #   ##  # #
    // #   #   #   #  ## #  ##   #   # # # #  ##
    // #   #   #   #   # #   #   #   #  ## #   #
    // ####  #####  ####  #### ##### #   #  ####


    public static void digColumn(World world, BlockPos pos, int radius, int minY, boolean drop)
    {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        // This is the center.  Dig out a 3x3
        while (y > minY)
        {
            for (int tmpX = x - radius; tmpX <= x + radius; ++tmpX)
            {
                for (int tmpZ = z - radius; tmpZ <= z + radius; ++tmpZ)
                {
                    world.destroyBlock(new BlockPos(tmpX, y, tmpZ), drop);
                }
            }
            --y;
        }
    }

    public static void spiralStairs(World world, BlockPos pos, int minY)
    {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        IBlockState state = Blocks.cobblestone.getDefaultState();

        int[] xOffset = { 0, 1, 1, 1, 0, -1, -1, -1 };
        int[] zOffset = { -1, -1, 0, 1, 1, 1, 0, -1 };

        outerLoop:
        while (y > minY)
        {
            for (int i = 0; i < xOffset.length; ++i)
            {
                int tmpX = x + xOffset[i];
                int tmpZ = z + zOffset[i];
                --y;
                if (y == minY)
                    break outerLoop;
                world.setBlockState(new BlockPos(tmpX, y, tmpZ), state);
            }
        }
    }

    /**
     * Puts dual slab stairs in a 7x7 area.  Corner platforms at top, positions 3 & 5 at half.
     * @param world The world.
     * @param pos Center
     * @param minY How low to go.
     */

    public static void bigSpiralStairs(World world, BlockPos pos, int minY)
    {
        IBlockState topState = Blocks.stone_slab.getDefaultState().
                withProperty(BlockStoneSlab.VARIANT, BlockStoneSlab.EnumType.COBBLESTONE).
                withProperty(BlockStoneSlab.HALF, BlockSlab.EnumBlockHalf.TOP);

        IBlockState bottomState = Blocks.stone_slab.getDefaultState().
                withProperty(BlockStoneSlab.VARIANT, BlockStoneSlab.EnumType.COBBLESTONE).
                withProperty(BlockStoneSlab.HALF, BlockSlab.EnumBlockHalf.BOTTOM);

        int y = pos.getY();
        BlockWalker walker = new BlockWalker(new BlockPos(pos.getX() - 3, pos.getY(), pos.getZ() - 3), false);
        while (y > minY)
        {
            // Make pad
            fill(world, walker.getRow(0, 1), topState);
            walker.forward();
            fill(world, walker.getRow(0, 1), topState);

            // Make descent
            for (int i = 0; i < 3; ++i)
            {
                walker.forward();
                y = walker.downHalf();
                if (y <= minY)
                    return;

                fill(world, walker.getRow(0, 1), walker.isDown() ? bottomState : topState);
            }

            // Get down off stair
            walker.forward();
            y = walker.downHalf();

            // Move to corner for next pad
            walker.forward();

            // Turn and do next section
            walker.turnRight();
        }
    }

    private static void fill(World world, BlockPos[] blocks, IBlockState state)
    {
        for (BlockPos pos : blocks)
        {
            world.setBlockState(pos, state);
        }
    }

    //=============================================================================================

    /**
     * For a clockwise traveller, looks at the current relative position and returns the
     * direction of the traveller.
     * @param center The center of the area.
     * @param pos The position to test for
     * @return Direction
     */
    public static COMPASS findClockwiseDir(BlockPos center, BlockPos pos)
    {
        // SIDE:             EAST           SOUTH         WEST           NORTH
        COMPASS[] mapping = {COMPASS.SOUTH, COMPASS.WEST, COMPASS.NORTH, COMPASS.EAST };
        return mapping[determineCompassDirection(center, pos).ordinal()];
    }

    /**
     * Determines the basic heading.
     * @param center The center to look for.
     * @param pos The current position.
     * @return The compass direction.
     */
    public static COMPASS determineCompassDirection(BlockPos center, BlockPos pos)
    {
        // Look at slop and x/y
        // -/- | +/-
        // ----+-----
        // -/+ | +/+

        // \  >1   /
        //  \  |  /
        //   \ | /
        // <1  *  < 1
        //   / | \
        //  /  |  \
        // /  >1   \

        // Slope is dZ/dX

        int deltaX = pos.getX() - center.getX();
        int deltaZ = pos.getZ() - center.getZ();

        // If we have no delta X, we are above or below.
        if (deltaX == 0)
            return (deltaZ > 0) ? COMPASS.SOUTH : COMPASS.NORTH;

        // Use slope
        double slope = Math.abs(deltaZ * 1.0D) / Math.abs(deltaX * 1.0D);
        if (slope > 0)
            return (deltaZ > 0) ? COMPASS.SOUTH : COMPASS.NORTH;
        else
            return (deltaX > 0) ? COMPASS.EAST : COMPASS.WEST;
    }

    private static final Logger LOGGER = LogManager.getLogger();
}
