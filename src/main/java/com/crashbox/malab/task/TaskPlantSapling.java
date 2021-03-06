package com.crashbox.malab.task;

import com.crashbox.malab.util.MALUtils;
import com.crashbox.malab.ai.EntityAIWorkDroid;
import com.crashbox.malab.messaging.TRPlantSapling;
import com.crashbox.malab.task.ITask.UpdateResult;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;

import java.util.List;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class TaskPlantSapling extends TaskDeliverBase
{
    public TaskPlantSapling(EntityAIWorkDroid performer, TRPlantSapling message)
    {
        super(performer, message.getSender(), message.getValue());
        _matcher = message.getMatcher();
    }

    @Override
    public BlockPos getWorkTarget(List<BlockPos> others)
    {
        _plantingTarget = MALUtils.findEmptyOrchardSquare(getWorld(), getRequester().getBlockPos(),
                getRequester().getRadius());
        return _plantingTarget;
    }

    @Override
    public UpdateResult executeAndIsDone()
    {
        // Plant
        ItemStack held = getEntity().getHeldItem();
        if (held == null)
            return UpdateResult.DONE;

        IBlockState state = Blocks.sapling.getStateFromMeta(held.getMetadata());
        getWorld().setBlockState(_plantingTarget, state);

        // Get next sapling
        held.stackSize--;
        if (held.stackSize <= 0)
        {
            getEntity().setCurrentItemOrArmor(0, null);
            return UpdateResult.DONE;
        }

        return UpdateResult.RETARGET;
    }

    //===================================

    @Override
    public void debugInfo(StringBuilder builder)
    {
        super.debugInfo(builder);
        builder.append(", platingTarget=").append(_plantingTarget);
    }

    private BlockPos _plantingTarget;

}
