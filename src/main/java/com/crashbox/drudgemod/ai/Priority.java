package com.crashbox.drudgemod.ai;

import com.crashbox.drudgemod.task.TaskBase;
import net.minecraft.util.BlockPos;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class Priority
{



    public static int computeDistanceCost(BlockPos startPos, BlockPos endPos)
    {
        return ( (int) Math.sqrt(startPos.distanceSq(endPos)) ) / 10;
    }
}
