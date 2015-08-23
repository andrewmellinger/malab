package com.crashbox.drudgemod.ai;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;
import java.util.List;

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

    public static BlockPos findFirstEntity(World world, BlockPos startPos, int range, Block blockType, List<TaskBase> exclude )
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
                if ( entityItem.getEntityItem().getItem() == Item.getItemFromBlock(blockType) )
                {
                    return entityItem.getPosition();
                }
            }
        }
        return null;
    }

    public static BlockPos findBlock(World world, BlockPos startPos, int range, Block blockType, List<TaskBase> exclude )
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
                        // Make sure we don't already have a task focused on this block
                        boolean foundOne = false;
                        for (TaskBase task : exclude)
                        {
                            if (task.getFocusBlock() != null && task.getFocusBlock().equals(target))
                            {
                                foundOne = true;
                                break;
                            }
                        }

                        // Since we got here we are okay
                        if (!foundOne)
                            return target;
                    }
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
