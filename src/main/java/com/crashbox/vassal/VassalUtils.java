package com.crashbox.vassal;

import com.crashbox.vassal.common.ItemStackMatcher;
import com.crashbox.vassal.util.MutablePos;
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

    // Default order is clockwise
    public static enum WALKER_FACING { EAST, SOUTH, WEST, NORTH }
    public static class BlockWalker
    {
        public BlockWalker(BlockPos current, boolean down)
        {
            _current = new MutablePos(current.getX(), current.getY(), current.getZ());
            _down = down;
        }

        public BlockPos getPos()
        {
            return _current.makeBlockPos();
        }

        public void forward()
        {
            CLOCKWISE[_direction].forward(_current);
        }

        public void turnLeft()
        {
            _direction -= 1;
            if (_direction == -1)
                _direction = 3;
        }

        public void turnRight()
        {
            _direction += 1;
            if (_direction == 4)
                _direction = 0;
        }

        public int down()
        {
            _current._y -= 1;
            return _current._y;
        }

        public int downHalf()
        {
            if (_down)
                _current._y -= 1;
            _down = !_down;
            return _current._y;
        }

        public boolean isDown()
        {
            return _down;
        }

        /**
         * Gets a slab of blocks based on our current direction, extending the specified
         * number to the left and right.  They will all be at the same height
         * @param left Number of blocks to the left.
         * @param right Number of blocks to the right.
         * @return List of blocks.
         */
        public BlockPos[] getRow(int left, int right)
        {
            BlockPos[] result = new BlockPos[left + 1 + right];

            int idx = 0;
            for (int i = left; i > 0; --i)
            {
                result[i] = CLOCKWISE[_direction].left(_current, i);
                ++idx;
            }

            result[idx] = _current.makeBlockPos();
            ++idx;

            for (int i = 1; i <= right; ++i)
            {
                result[i] = CLOCKWISE[_direction].right(_current, i);
                ++idx;
            }

            return result;
        }

        private int _direction = 0;
        private MutablePos _current;
        private boolean _down;
    }

    public static Incrementer[] CLOCKWISE = { new IncrementerEast(),
                                              new IncrementerSouth(),
                                              new IncrementerWest(),
                                              new IncrementerNorth() };

    public static abstract class Incrementer
    {
        public abstract void forward(MutablePos pos);
        public abstract BlockPos left(MutablePos pos, int offset);
        public abstract BlockPos right(MutablePos pos, int offset);
    }

    public static class IncrementerEast extends Incrementer
    {
        @Override
        public void forward(MutablePos pos)                { pos._x += 1; }

        @Override
        public BlockPos left(MutablePos pos, int offset)   { return pos.makeOffset( 0, 0, offset * -1 ); }

        @Override
        public BlockPos right(MutablePos pos, int offset)  { return pos.makeOffset( 0, 0, offset ); }
    }

    public static class IncrementerSouth extends Incrementer
    {
        @Override
        public void forward(MutablePos pos)                { pos._z += 1; }

        @Override
        public BlockPos left(MutablePos pos, int offset)   { return pos.makeOffset( offset, 0, 0 ); }

        @Override
        public BlockPos right(MutablePos pos, int offset)  { return pos.makeOffset( offset * -1, 0, 0 ); }
    }

    public static class IncrementerWest extends Incrementer
    {
        @Override
        public void forward(MutablePos pos)                { pos._x -= 1; }

        @Override
        public BlockPos left(MutablePos pos, int offset)   { return pos.makeOffset( 0, 0, offset ); }

        @Override
        public BlockPos right(MutablePos pos, int offset)  { return pos.makeOffset( 0, 0, offset * -1 ); }
    }

    public static class IncrementerNorth extends Incrementer
    {
        @Override
        public void forward(MutablePos pos)                { pos._z -= 1; }

        @Override
        public BlockPos left(MutablePos pos, int offset)   { return pos.makeOffset( offset * -1, 0, 0 ); }

        @Override
        public BlockPos right(MutablePos pos, int offset)  { return pos.makeOffset( offset, 0, 0 ); }
    }


    private static final Logger LOGGER = LogManager.getLogger();
}
