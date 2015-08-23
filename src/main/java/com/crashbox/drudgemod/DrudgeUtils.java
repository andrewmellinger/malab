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

    public static boolean sameType(World world, BlockPos pos, ItemStack stack)
    {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (Block.getBlockFromItem(stack.getItem()) == block)
        {
            // If log, ignore lowest two bits
            if (stack.getItem() == Item.getItemFromBlock(Blocks.log) ||
                    stack.getItem() == Item.getItemFromBlock(Blocks.log2))
            {

                int meta = block.getMetaFromState(state);


                LOGGER.debug("State: " + state);
                LOGGER.debug("meta: " + meta);

                meta = meta & 0x3;

                if (meta == (stack.getMetadata() & 0x3))
                {
                    return true;
                }
            }


            return block.getMetaFromState(state) == stack.getMetadata();
        }

        return false;
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
        // TODO:  Check all tool pre-requisites

        if (stack.stackSize >= stack.getMaxStackSize())
            return false;

        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        // TODO Animate Breaking
        //block.getHarvestLevel(state);
        //block.harvestBlock();

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
