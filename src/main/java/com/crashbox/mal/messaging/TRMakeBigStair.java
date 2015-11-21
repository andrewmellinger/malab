package com.crashbox.mal.messaging;

import com.crashbox.mal.common.ItemStackMatcher;
import com.crashbox.mal.task.TaskMakeBigStair;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStoneSlab;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

/**
 * Copyright CMU 2015.
 */
public class TRMakeBigStair extends TRDeliverBase
{
    public TRMakeBigStair(IMessager sender, IMessager receiver, Object transactionID, int value, int quantity)
    {
        super(sender, receiver, transactionID, value, TaskMakeBigStair.class,
                getStairMatcher(), quantity);
    }

    private static ItemStackMatcher getStairMatcher()
    {
        ItemStackMatcher matcher = new ItemStackMatcher();
        matcher.add(Blocks.cobblestone);

        IBlockState state = Blocks.stone_slab.getDefaultState().
                withProperty(BlockStoneSlab.VARIANT, BlockStoneSlab.EnumType.COBBLESTONE);

        Block block = Blocks.stone_slab;
        if (block instanceof BlockStoneSlab)
            matcher.add(new ItemStack(Blocks.stone_slab, 0, block.getMetaFromState(state)));

        return matcher;
    }

}
