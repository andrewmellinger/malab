package com.crashbox.malab.task;

import com.crashbox.malab.util.MALUtils;
import com.crashbox.malab.ai.EntityAIWorkDroid;
import com.crashbox.malab.messaging.TRHarvestBlock;
import net.minecraft.util.BlockPos;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Copyright CMU 2015.
 */
public class TaskHarvestBlock extends TaskHarvest
{
    public TaskHarvestBlock(EntityAIWorkDroid performer, TRHarvestBlock message)
    {
        super(performer, message);
        _pos = message.getPos();
    }

    @Override
    protected Queue<BlockPos> findHarvestList(List<BlockPos> exclusions)
    {
        Queue<BlockPos> result = new LinkedList<BlockPos>();
        if (!getWorld().isAirBlock(_pos) &&
                MALUtils.willDrop(getWorld(), _pos, getMatcher()))
        {
            result.add(_pos);
        }
        return result;
    }

    private final BlockPos _pos;
}
