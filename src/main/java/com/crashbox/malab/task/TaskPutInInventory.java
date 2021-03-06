package com.crashbox.malab.task;

import com.crashbox.malab.ai.EntityAIWorkDroid;
import com.crashbox.malab.autoblock.TileEntityAutoBlockInventory;
import com.crashbox.malab.common.ItemStackMatcher;
import com.crashbox.malab.messaging.*;
import com.crashbox.malab.task.ITask.UpdateResult;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Copyright 2015 Andrew O. Mellinger
 * <p/>
 * In this task we take the item(s) in our inventory to the target destination.
 */
public class TaskPutInInventory extends TaskDeliverBase
{
    @Override
    public UpdateResult executeAndIsDone()
    {
        ItemStack current = getEntity().getHeldItem();

        IMessager requester = getRequester();
        if (requester instanceof TileEntityAutoBlockInventory)
        {
            current = ((TileEntityAutoBlockInventory) requester).mergeIntoBestSlot(current);
            getEntity().setCurrentItemOrArmor(0, current);
        }
        else
        {
            getEntity().dropHeldItem();
            LOGGER.warn("Could not deliver item to target. It isn't an inventory: " + getRequester());
        }

        return UpdateResult.DONE;
    }

    public TaskPutInInventory(EntityAIWorkDroid performer, TRPutInInventory message)
    {
        super(performer, message.getSender(), message.getValue());
        _matcher = message.getMatcher();
        _quantity = message.getQuantity();
    }

    public ItemStackMatcher getMatcher()
    {
        return _matcher;
    }

    public int getQuantity()
    {
        return _quantity;
    }

    @Override
    public BlockPos getWorkTarget(List<BlockPos> others)
    {
        // We just work here.
        return getRequester().getBlockPos();
    }

    @Override
    public void debugInfo(StringBuilder builder)
    {
        super.debugInfo(builder);
        builder.append(", itemSample=").append(_matcher);
        builder.append(", quantity=").append(_quantity);
    }

    private final ItemStackMatcher _matcher;
    private final int _quantity;

    private static final Logger LOGGER = LogManager.getLogger();
}
