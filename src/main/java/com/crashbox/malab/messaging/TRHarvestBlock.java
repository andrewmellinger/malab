package com.crashbox.malab.messaging;

import com.crashbox.malab.common.ItemStackMatcher;
import com.crashbox.malab.task.TaskHarvestBlock;
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
