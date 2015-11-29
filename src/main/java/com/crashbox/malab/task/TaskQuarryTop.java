package com.crashbox.malab.task;

import com.crashbox.malab.ai.EntityAIWorkDroid;
import com.crashbox.malab.messaging.TRHarvest;
import com.crashbox.malab.util.StairBuilder;
import net.minecraft.util.BlockPos;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class TaskQuarryTop extends TaskHarvest
{
    public TaskQuarryTop(EntityAIWorkDroid performer, TRHarvest message)
    {
        super(performer, message);
        _builder = new StairBuilder(getWorld(), getRequester().getBlockPos(), getRequester().getRadius());
    }

    @Override
    protected Queue<BlockPos> findHarvestList(List<BlockPos> exclusions)
    {
        Queue<BlockPos> list = new LinkedList<BlockPos>();
        BlockPos pos = _builder.findTopQuarryable(getMatcher(), _performer.getEntity(), exclusions);
        if (pos != null)
            list.add(pos);

        return list;
    }

    private final StairBuilder _builder;
}

