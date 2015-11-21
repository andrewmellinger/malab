package com.crashbox.mal.task;

import com.crashbox.mal.util.VassalUtils;
import com.crashbox.mal.ai.EntityAIVassal;
import com.crashbox.mal.common.ItemStackMatcher;
import com.crashbox.mal.messaging.IMessager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;

import java.util.List;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class TaskRefuel extends TaskDeliverBase
{
    public TaskRefuel(EntityAIVassal performer, IMessager requester,
                      ItemStackMatcher matcher, int quantity)
    {
        super(performer, requester, 1000);
        _matcher = matcher;
        _quantity = quantity;

    }

    @Override
    public BlockPos getWorkTarget(List<BlockPos> others)
    {
        // Do it right where we are standing
        return getEntity().getPosition();
    }

    @Override
    public ITask.UpdateResult executeAndIsDone()
    {
        ItemStack held = getEntity().getHeldItem();
        ItemStack fuel = getEntity().getFuelStack();

        if (held == null || held.stackSize == 0)
            return ITask.UpdateResult.DONE;

        if (fuel == null || fuel.stackSize == 0)
        {
            getEntity().setCurrentItemOrArmor(0, null);
            getEntity().setFuelStack(held);
            return ITask.UpdateResult.DONE;
        }

        VassalUtils.mergeStacks(fuel, held);
        if (held.stackSize == 0)
            getEntity().setCurrentItemOrArmor(0, null);

        return ITask.UpdateResult.DONE;
    }
}
