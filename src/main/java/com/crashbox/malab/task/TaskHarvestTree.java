package com.crashbox.malab.task;

import com.crashbox.malab.util.MALUtils;
import com.crashbox.malab.ai.EntityAIWorkDroid;
import com.crashbox.malab.util.BlockBounds;
import com.crashbox.malab.util.RingedSearcher;
import com.crashbox.malab.messaging.TRHarvest;
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
