package com.crashbox.vassal.task;

import com.crashbox.vassal.ai.EntityAIVassal;
import com.crashbox.vassal.messaging.TRHarvest;
import com.crashbox.vassal.util.StairBuilder;
import net.minecraft.util.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class TaskQuarry extends TaskHarvest
{
    public TaskQuarry(EntityAIVassal performer, TRHarvest message)
    {
        super(performer, message);
        _builder = new StairBuilder(getWorld(), getRequester().getBlockPos(), getRequester().getRadius());
    }


    @Override
    protected Queue<BlockPos> findHarvestList(List<BlockPos> exclusions)
    {
        Queue<BlockPos> list = new LinkedList<BlockPos>();
        //BlockPos pos = _builder.findFirstQuarryable(getMatcher(), (ItemTool) Items.stone_pickaxe);
        BlockPos pos = _builder.findFirstQuarryable(getMatcher(), _performer.getEntity(), exclusions);
//        BlockPos pos = _builder.findFirstQuarryable(getMatcher(), null, exclusions);
        if (pos != null)
            list.add(pos);
        else
            LOGGER.debug("findHarvestList couldn't find a block." + getRequester().getBlockPos());
        return list;
    }

    @Override
    protected void onBlockBroken(BlockPos pos)
    {
        // If the one below is there, try to place inventory
        BlockPos downOne = pos.down();
        if (getWorld().isAirBlock(downOne))
            getEntity().placeHeldBlock(getWorld(), downOne);
    }

    private final StairBuilder _builder;

    private static final Logger LOGGER = LogManager.getLogger();
}
