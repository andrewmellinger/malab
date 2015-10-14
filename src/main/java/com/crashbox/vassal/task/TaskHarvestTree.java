package com.crashbox.vassal.task;

import com.crashbox.vassal.util.VassalUtils;
import com.crashbox.vassal.ai.EntityAIVassal;
import com.crashbox.vassal.util.BlockBounds;
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
        BlockBounds bounds = new BlockBounds(getRequester().getBlockPos(), _radius);
        BlockPos start = VassalUtils.findIntersect(getRequester().getBlockPos(), _radius, getEntity().getPosition());
        return RingedSearcher.findTree(getEntity().getEntityWorld(), start, _radius * 2, _height,
                getMatcher(), others, bounds);
    }
}
