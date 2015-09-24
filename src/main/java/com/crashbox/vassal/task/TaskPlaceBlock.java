package com.crashbox.vassal.task;

import com.crashbox.vassal.ai.EntityAIVassal;
import com.crashbox.vassal.messaging.TRPlaceBlock;
import com.crashbox.vassal.task.ITask.UpdateResult;
import net.minecraft.util.BlockPos;

import java.util.List;

/**
 * Copyright CMU 2015.
 */
public class TaskPlaceBlock extends TaskDeliverBase
{
    public TaskPlaceBlock(EntityAIVassal performer, TRPlaceBlock message)
    {
        super(performer, message.getSender(), message.getValue());
        _pos = message.getPos();
    }

    @Override
    public BlockPos getWorkTarget(List<BlockPos> others)
    {
        if (getWorld().isAirBlock(_pos))
            return _pos;

        return null;
    }

    @Override
    public UpdateResult executeAndIsDone()
    {
        if (getWorld().isAirBlock(_pos))
            getEntity().placeHeldBlock(getWorld(), _pos);
        return UpdateResult.DONE;
    }

    private final BlockPos _pos;

}
