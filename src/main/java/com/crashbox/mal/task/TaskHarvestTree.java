package com.crashbox.mal.task;

import com.crashbox.mal.util.MALUtils;
import com.crashbox.mal.ai.EntityAIWorkDroid;
import com.crashbox.mal.util.BlockBounds;
import com.crashbox.mal.util.RingedSearcher;
import com.crashbox.mal.messaging.TRHarvest;
import net.minecraft.util.BlockPos;

import java.util.List;
import java.util.Queue;

/**
 * Copyright CMU 2015.
 */
public class TaskHarvestTree extends TaskHarvest
{
    public TaskHarvestTree(EntityAIWorkDroid performer, TRHarvest message)
    {
        super(performer, message);
    }

    @Override
    protected Queue<BlockPos> findHarvestList(List<BlockPos> exclusions)
    {
        BlockBounds bounds = new BlockBounds(getRequester().getBlockPos(), _radius);
        BlockPos start = MALUtils.findIntersect(getRequester().getBlockPos(), _radius, getEntity().getPosition());

        // We do a searcher times two because we are bounded by the bounding box
        // We start down so that we can get the area.
        return RingedSearcher.findTree(getEntity().getEntityWorld(), start.down(5), _radius * 2, _height + 5,
                getMatcher(), exclusions, bounds);
    }
}
