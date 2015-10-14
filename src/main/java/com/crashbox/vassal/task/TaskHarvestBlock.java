package com.crashbox.vassal.task;

import com.crashbox.vassal.util.VassalUtils;
import com.crashbox.vassal.ai.EntityAIVassal;
import com.crashbox.vassal.messaging.TRHarvestBlock;
import net.minecraft.util.BlockPos;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Copyright CMU 2015.
 */
public class TaskHarvestBlock extends TaskHarvest
{
    public TaskHarvestBlock(EntityAIVassal performer, TRHarvestBlock message)
    {
        super(performer, message);
        _pos = message.getPos();
    }

    @Override
    protected Queue<BlockPos> findHarvestList(List<BlockPos> others)
    {
        Queue<BlockPos> result = new LinkedList<BlockPos>();
        if (!getWorld().isAirBlock(_pos) &&
                VassalUtils.willDrop(getWorld(), _pos, getMatcher()))
        {
            result.add(_pos);
        }
        return result;
    }

    private final BlockPos _pos;
}
