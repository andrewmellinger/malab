package com.crashbox.vassal.task;

import com.crashbox.vassal.ai.EntityAIVassal;
import com.crashbox.vassal.util.RingedSearcher;
import com.crashbox.vassal.messaging.TRHarvest;
import net.minecraft.util.BlockPos;

import java.util.List;
import java.util.Queue;

/**
 * Copyright CMU 2015.
 */
public class TaskHarvestTree extends TaskHarvest
{
    public TaskHarvestTree(EntityAIVassal performer, TRHarvest message)
    {
        super(performer, message);
    }

    @Override
    protected Queue<BlockPos> findHarvestList(List<BlockPos> others)
    {
        return RingedSearcher.findTree(getEntity().getEntityWorld(), getRequester().getBlockPos(), _radius,
                _height, getMatcher(), others);
    }
}
