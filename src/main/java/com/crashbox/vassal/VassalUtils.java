package com.crashbox.vassal;

import com.crashbox.vassal.common.ItemStackMatcher;
import com.crashbox.vassal.entity.EntityVassal;
import com.crashbox.vassal.util.BlockWalker;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStoneSlab;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class VassalUtils
{

    public static BlockPos findBlock(World world, BlockPos startPos, int range, Block blockType )
    {
        for ( int x = startPos.getX() - range; x < startPos.getX() + range; ++x)
        {
            for ( int y = startPos.getY() - range; y < startPos.getY() + range; ++y)
            {
                for ( int z = startPos.getZ() - range; z < startPos.getZ() + range; ++z)
                {
                    BlockPos target = new BlockPos(x, y, z);
                    IBlockState state = world.getBlockState(target);
                    if (state.getBlock() == blockType)
                    {
                        return target;
                    }
                }
            }
        }
        return null;
    }

    public static BlockPos firstDropOccurrence(World world, BlockPos center, int radius,
            ItemStackMatcher matcher)
    {
        BlockPos start = new BlockPos(center.getX() - radius, center.getY(), center.getZ() - radius);
        BlockPos end = new BlockPos(center.getX() + radius, center.getY() - 1, center.getZ() + radius);
        return firstDropOccurrence(world, start, end, matcher);
    }

    public static BlockPos firstDropOccurrence(World world, BlockPos startPos, BlockPos endPos,
            ItemStackMatcher matcher)
    {
        int deltaX = startPos.getX() < endPos.getX() ? 1 : -1;
        int deltaY = startPos.getY() < endPos.getY() ? 1 : -1;
        int deltaZ = startPos.getZ() < endPos.getZ() ? 1 : -1;

        int endX = endPos.getX() + deltaX;
        int endY = endPos.getY() + deltaY;
        int endZ = endPos.getZ() + deltaZ;

        for ( int y = startPos.getY(); y != endY; y += deltaY)
        {
            for ( int x = startPos.getX(); x != endX; x += deltaX)
            {
                for ( int z = startPos.getZ(); z != endZ; z += deltaZ)
                {
                    BlockPos target = new BlockPos(x, y, z);
                    if (willDrop(world, target, matcher))
                        return target;
                }
            }
        }
        return null;
    }

    public static void collectEntityIntoStack(World world, BlockPos startPos, int range, ItemStack targetStack )
    {
        int x = startPos.getX();
        int y = startPos.getY();
        int z = startPos.getZ();

        AxisAlignedBB scanBlock = new AxisAlignedBB(x - range, y - range, z - range, x + range, y + (range * 3), z + range);
        List entities = world.getEntitiesWithinAABB(EntityItem.class, scanBlock);
        for (Object obj :entities)
        {
            if ( obj instanceof EntityItem)
            {
                EntityItem entityItem = (EntityItem) obj;
                if ( entityItem.getEntityItem().isItemEqual(targetStack) )
                {
                    world.removeEntity(entityItem);
                    targetStack.stackSize += entityItem.getEntityItem().stackSize;
                }
            }
        }
    }

    /**
     * Searches the area for items of the base item type ignoring metadata.  The first item found sets
     * the subsequent search parameters.  For example, if the first stack contains oak blocks, then it
     * will only find oak blocks and not birch blocks.
     * @param world The world in which to search.
     * @param startPos Starting point (center)
     * @param range The range.
     * @param itemType The basic item type to look for.
     * @return An itemstack containing those items.  If nothing is found, the stack will have size zero.
     */
    public static ItemStack collectEntityIntoNewStack(World world, BlockPos startPos, int range, Item itemType )
    {
        int x = startPos.getX();
        int y = startPos.getY();
        int z = startPos.getZ();
        ItemStack targetStack = null;

        AxisAlignedBB scanBlock = new AxisAlignedBB(x - range, y - range, z - range, x + range, y + range, z + range);
        List entities = world.getEntitiesWithinAABB(EntityItem.class, scanBlock);
        for (Object obj :entities)
        {
            if ( obj instanceof EntityItem)
            {
                EntityItem entityItem = (EntityItem) obj;
                if (targetStack == null)
                {
                    if (entityItem.getEntityItem().getItem() == itemType)
                    {
                        targetStack = entityItem.getEntityItem().copy();
                        world.removeEntity(entityItem);
                    }
                }
                else
                {
                    if (entityItem.getEntityItem().isItemEqual(targetStack))
                    {
                        targetStack.stackSize += entityItem.getEntityItem().stackSize;
                        world.removeEntity(entityItem);
                    }
                }
            }
        }

        if (targetStack == null)
            targetStack = new ItemStack(itemType, 0);

        return targetStack;
    }

    /**
     * Counts the number of Vassal entities in the specific area.
     * @param world The world object
     * @param center The center of the search area.
     * @param range The range in which to search.
     * @return Number of EntityVassal found.
     */
    public static int countVassalsInArea(World world, BlockPos center, int range)
    {
        int x = center.getX();
        int y = center.getY();
        int z = center.getZ();

        AxisAlignedBB scanBlock = new AxisAlignedBB(x - range, y - 4, z - range, x + range, y + 20, z + range);
        List entities = world.getEntitiesWithinAABB(EntityVassal.class, scanBlock);
        return entities.size();
    }

    /**
     * Searches the area for items of the base item type ignoring metadata.  The first item found sets
     * the subsequent search parameters.  For example, if the first stack contains oak blocks, then it
     * will only find oak blocks and not birch blocks.
     * @param world The world in which to search.
     * @param startPos Starting point (center)
     * @param range The range.
     * @param itemType The basic item type to look for.
     * @return An list of all the entities containing those items.  If nothing is found, the stack will have size zero.
     */
    public static Queue<EntityItem> findEntitiesOfType(World world, BlockPos startPos, int range, Item itemType )
    {
        int x = startPos.getX();
        int y = startPos.getY();
        int z = startPos.getZ();
        Queue<EntityItem> results = new LinkedList<EntityItem>();
        ItemStack targetStack = null;

        AxisAlignedBB scanBlock = new AxisAlignedBB(x - range, y - range, z - range, x + range, y + range, z + range);
        List entities = world.getEntitiesWithinAABB(EntityItem.class, scanBlock);
        for (Object obj :entities)
        {
            if ( obj instanceof EntityItem)
            {
                EntityItem entityItem = (EntityItem) obj;
                if (targetStack == null)
                {
                    if (entityItem.getEntityItem().getItem() == itemType)
                    {
                        targetStack = entityItem.getEntityItem().copy();
                        results.add(entityItem);
                    }
                }
                else
                {
                    if (entityItem.getEntityItem().isItemEqual(targetStack))
                    {
                        results.add(entityItem);
                    }
                }
            }
        }

        return results;
    }

    /**
     * Searches the area for items of the base item type ignoring metadata.  The first item found sets
     * the subsequent search parameters.  For example, if the first stack contains oak blocks, then it
     * will only find oak blocks and not birch blocks.
     * @param world The world in which to search.
     * @param startPos Starting point (center)
     * @param range The range.
     * @param itemType The basic item type to look for.
     * @return An list of all the entities containing those items.  If nothing is found, the stack will have size zero.
     */
    public static EntityItem findFirstEntityOfType(World world, BlockPos startPos, int range, Item itemType )
    {
        int x = startPos.getX();
        int y = startPos.getY();
        int z = startPos.getZ();

        AxisAlignedBB scanBlock = new AxisAlignedBB(x - range, y - range, z - range, x + range, y + range, z + range);
        List entities = world.getEntitiesWithinAABB(EntityItem.class, scanBlock);
        for (Object obj :entities)
        {
            if ( obj instanceof EntityItem)
            {
                EntityItem entityItem = (EntityItem) obj;
                if (entityItem.getEntityItem().getItem() == itemType)
                {
                    return entityItem;
                }
            }
        }

        return null;
    }

    /**
     * Searches the area for items of the base item type ignoring metadata.  The first item found sets
     * the subsequent search parameters.  For example, if the first stack contains oak blocks, then it
     * will only find oak blocks and not birch blocks.
     * @param world The world in which to search.
     * @param startPos Starting point (center)
     * @param range The range.
     * @param itemType The basic item type to look for.
     * @return An list of all the entities containing those items.  If nothing is found, the stack will have size zero.
     */
    public static EntityItem findFirstEntityOfTypeOnGround(World world, BlockPos startPos, int range, Item itemType )
    {
        int x = startPos.getX();
        int y = startPos.getY();
        int z = startPos.getZ();

        AxisAlignedBB scanBlock = new AxisAlignedBB(x - range, y, z - range, x + range, y + 1, z + range);
        List entities = world.getEntitiesWithinAABB(EntityItem.class, scanBlock);
        for (Object obj :entities)
        {
            if ( obj instanceof EntityItem)
            {
                EntityItem entityItem = (EntityItem) obj;
                if (entityItem.getEntityItem().getItem() == itemType)
                {
                    return entityItem;
                }
            }
        }

        return null;
    }

    /**
     * Walks a checkboard pattern and finds the first empty (air) spot
     * @param world The world.
     * @param center Center of checkboard.
     * @param radius "Radius" (remember, this is a square
     * @return "First" (from -x, -z) position.
     */
    public static BlockPos findEmptyCheckerBoardSquare(World world, BlockPos center, int radius)
    {
        int startX = center.getX() - radius;
        int startZ = center.getZ() - radius;
        int width = radius + radius + 1;
        for (int z = startZ; z < startZ + width; ++z)
        {
            int tmpX = startX + (z - startZ) % 2;
            for ( int x = tmpX; x < startX + width; x += 2)
            {
                BlockPos pos = new BlockPos(x, center.getY(), z);
                if ( world.isAirBlock(pos) )
                {
                    return pos;
                }
            }
        }

        return null;
    }

    /**
     * Walks an "orchard" pattern around the center.  The patterns is rows
     * and columns every two blocks separated by a block.  This pattern is good
     * so bots can get through.
     * @param world The world.
     * @param center Center of ochard.
     * @param radius "Radius" (remember, this is a square)
     * @return "First" (from -x, -z) position.
     */
    public static BlockPos findEmptyOrchardSquare(World world, BlockPos center, int radius)
    {
        radius = radius - (radius % 2);
        for (int z = center.getZ() - radius; z <= center.getZ() + radius; z += 2)
        {
            for (int x = center.getX() - radius; x <= center.getX() + radius; x += 2)
            {
                BlockPos pos = new BlockPos(x, center.getY(), z);
                if ( world.isAirBlock(pos) )
                {
                    return pos;
                }
            }
        }

        return null;
    }

    public static enum COMPASS { EAST, SOUTH, WEST, NORTH }

    public static void showStack(Logger logger)
    {
        try
        {
            throw new Exception("Arg");
        }
        catch (Exception e)
        {
            for (StackTraceElement elem : e.getStackTrace())
            {
                logger.debug(elem);
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

    /**
     * Harvests the specific block if it matchers the  item stack and will put into the held
     * inventory if available.
     * @param world The  world object.
     * @param entity The entiity performing  the action.
     * @param harvestBlock The block to harvest.
     * @param matcher The matcher describing  allowable drops.
     * @return True if harvested.
     */
    public static boolean harvestBlockIntoHeld(World world, EntityVassal entity, BlockPos harvestBlock,
                                               ItemStackMatcher matcher)
    {
        return harvestBlockIntoHeld(world, entity, harvestBlock, matcher, false);
    }

        /**
         * Harvests the specific block if it matchers the  item stack and will put into the held
         * inventory if available.
         * @param world The  world object.
         * @param entity The entiity performing  the action.
         * @param harvestBlock The block to harvest.
         * @param matcher The matcher describing  allowable drops.
         * @param mustMatch True if must match.
         * @return True if harvested.
         */
    public static boolean harvestBlockIntoHeld(World world, EntityVassal entity, BlockPos harvestBlock,
                                               ItemStackMatcher matcher, boolean mustMatch)
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
            if (mustMatch)
            {
                if (!VassalUtils.willDrop(entity.getEntityWorld(), harvestBlock, new ItemStackMatcher(targetStack)))
                    return false;
            }
        }

        ///// BREAK
        world.destroyBlock(harvestBlock, true);

        ///// PICKUP
        collectEntityIntoStack(world, harvestBlock, 3, targetStack);

        if (entity.getHeldItem() == null && targetStack.stackSize > 0)
            entity.setCurrentItemOrArmor(0, targetStack);

        return true;
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

    public static BlockPos getBlockBeside(BlockPos start, BlockPos target)
    {
        return new BlockPos( target.getX() + comp(start.getX(), target.getX()),
                             target.getY(),
                             target.getZ() + comp(start.getZ(), target.getZ()));
    }

    public static int comp(int start, int end)
    {
        int x = 0;

        int xDelta = start - end;
        if (xDelta > 0)
            x = 1;
        else if (xDelta < 1)
            x = -1;

        return x;
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

    public static BlockPos[] getCorners(BlockPos center, int radius)
    {
        return new BlockPos[] {
                new BlockPos(center.getX() - radius, center.getY(), center.getZ() - radius),
                new BlockPos(center.getX() + radius, center.getY(), center.getZ() - radius),
                new BlockPos(center.getX() + radius, center.getY(), center.getZ() + radius),
                new BlockPos(center.getX() - radius, center.getY(), center.getZ() + radius)
            };
    }

    public static BlockPos nextCornerClockwise(BlockPos center, BlockPos corner)
    {
        BlockPos[] corners = getCorners(center, Math.abs(corner.getX() - center.getX()));
        for (int i = 0; i < 4; ++i)
        {
            if (corners[i].equals(corner))
                return corners[ (i + 1) %  4];
        }

        throw new RuntimeException("Couldn't find corner.");
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

                fill(world, walker.getRow(0, 1), walker.isHalf() ? bottomState : topState);
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
        // Look at slope and x/y
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
//        LOGGER.debug("deltaX=" + deltaX + ", deltaZ:" + deltaZ);

        // If we are on the axis, then we do the right thing
        if ( deltaX == deltaZ)
            return (deltaX > 0) ? COMPASS.WEST : COMPASS.EAST;
        else if (deltaX == deltaZ * -1)
            return (deltaX > 0) ? COMPASS.SOUTH : COMPASS.NORTH;

        // If we have no delta X, we are above or below.   Slope computations are bad(tm)
        if (deltaX == 0)
            return (deltaZ > 0) ? COMPASS.WEST : COMPASS.EAST;

        // Use slope
        double slope = Math.abs(deltaZ * 1.0D) / Math.abs(deltaX * 1.0D);
//        LOGGER.debug("slope=" + slope);
        if (slope > 1)
            return (deltaZ > 0) ? COMPASS.WEST : COMPASS.EAST;
        else
            return (deltaX > 0) ? COMPASS.SOUTH : COMPASS.NORTH;
    }

    private static final Logger LOGGER = LogManager.getLogger();
}
