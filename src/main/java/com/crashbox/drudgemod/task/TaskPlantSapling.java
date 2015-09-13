package com.crashbox.drudgemod.task;

import com.crashbox.drudgemod.ai.AIUtils;
import com.crashbox.drudgemod.ai.EntityAIDrudge;
import com.crashbox.drudgemod.messaging.MessagePlantSapling;
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
    public TaskPlantSapling(EntityAIDrudge performer, MessagePlantSapling message)
    {
        super(performer, message.getSender(), message.getValue());
    }

    @Override
    public BlockPos chooseWorkArea(List<BlockPos> others)
    {
        _plantingTarget = AIUtils.findEmptyOrchardSquare(getWorld(), getRequester().getPos(),
                getRequester().getRadius());
        return _plantingTarget;
    }

    @Override
    public boolean executeAndIsDone()
    {
        // Plant
        ItemStack held = getEntity().getHeldItem();
        IBlockState state = Blocks.sapling.getStateFromMeta(held.getMetadata());
        getWorld().setBlockState(_plantingTarget, state);

        // Get next sapling
        held.stackSize--;
        if (held.stackSize <= 0)
        {
            getEntity().setCurrentItemOrArmor(0, null);
        }

        return true;
    }

    @Override
    public int getValue()
    {
        // SWAG
        return _priority - 10;
    }

    //===================================

    @Override
    public void debugInfo(StringBuilder builder)
    {
        super.debugInfo(builder);
    }

    private BlockPos _plantingTarget;

    // State management
    private enum GoingTo { SITE, COLLECTION, PLANTING}
}
