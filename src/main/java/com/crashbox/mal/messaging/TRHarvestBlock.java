package com.crashbox.mal.messaging;

import com.crashbox.mal.common.ItemStackMatcher;
import com.crashbox.mal.task.TaskHarvestBlock;
import net.minecraft.util.BlockPos;

/**
 * Copyright CMU 2015.
 */
public class TRHarvestBlock extends TRHarvest
{
    public TRHarvestBlock(IMessager sender, IMessager target, Object transactionID,
                          int priority, ItemStackMatcher matcher, BlockPos pos)
    {
        super(sender, target, transactionID, priority, TaskHarvestBlock.class, matcher, 1);
        _pos = pos;
    }

    public BlockPos getPos()
    {
        return _pos;
    }

    private final BlockPos _pos;

}
