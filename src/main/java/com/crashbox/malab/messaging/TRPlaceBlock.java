package com.crashbox.malab.messaging;

import com.crashbox.malab.common.AnyItemMatcher;

import com.crashbox.malab.task.TaskPlaceBlock;
import net.minecraft.util.BlockPos;

/**
 * Copyright CMU 2015.
 */
public class TRPlaceBlock extends TRDeliverBase
{
    public TRPlaceBlock(IMessager sender, IMessager receiver, Object transactionID, int value,
                        BlockPos pos)
    {
        super(sender, receiver, transactionID, value, TaskPlaceBlock.class,
                new AnyItemMatcher(), 1);
        _pos = pos;
    }

    public BlockPos getPos()
    {
        return _pos;
    }

    private final BlockPos _pos;

}
