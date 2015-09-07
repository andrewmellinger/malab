package com.crashbox.drudgemod.ai;

import com.crashbox.drudgemod.task.TaskBase;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * Copyright 2015 Andrew o. Mellinger
 */
public class AIUtils
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

    public static void collectEntityIntoStack(World world, BlockPos startPos, int range, ItemStack targetStack )
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


    public static interface BlockVisitor
    {
        /** @return true to keep visiting */
        public boolean visit(BlockPos pos);
    }

    /**
     * Visits the blocks in a cubic shell surrounding the area.
     * @param world
     * @param center
     * @param start
     * @param quantity
     * @param filter
     * @return
     */
    public static BlockPos visitBlock(World world, BlockPos center, BlockPos start, int quantity, BlockVisitor filter)
    {
        return null;
    }

    /**
     * Finds trees in the area, starting at the specified block at ground level and working outward.
     * We assume vertical blocks are trees so we work that way.
     */
    public static List<BlockPos> findTree(World world, BlockPos center, int radius, int height, int quantity, Block type)
    {
        // Search in ring at top, then work down if we find a block

        int x;
        int y;
        int z;



        return null;
    }

    private static final Logger LOGGER = LogManager.getLogger();
}
