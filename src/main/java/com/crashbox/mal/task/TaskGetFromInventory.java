package com.crashbox.mal.task;

import com.crashbox.mal.ai.EntityAIWorkDroid;
import com.crashbox.mal.autoblock.TileEntityAutoBlockInventory;
import com.crashbox.mal.messaging.TRGetFromInventory;
import com.crashbox.mal.task.ITask.UpdateResult;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class TaskGetFromInventory extends TaskAcquireBase
{
    public TaskGetFromInventory(EntityAIWorkDroid performer, TRGetFromInventory message)
    {
        super(performer, message.getSender(), message.getValue(), message.getMatcher());

        int quantity = message.getQuantity();
        if (quantity > getPerformer().getEntity().getCarryCapacity())
            quantity = getPerformer().getEntity().getCarryCapacity();
        _quantity = quantity;

        _workArea = getRequester().getBlockPos();
    }

    @Override
    public BlockPos getWorkTarget(List<BlockPos> others)
    {
        // We only do this once.
        return _workArea;
    }

    @Override
    public UpdateResult executeAndIsDone()
    {
        _workArea = null;

        // Get the tile entity and extract it
        //debugLog(LOGGER, "Going to extract items. ");
        TileEntity entity = getWorld().getTileEntity(getRequester().getBlockPos());
        if (entity instanceof TileEntityAutoBlockInventory)
        {
            //debugLog(LOGGER, "Extracting: entity=" + entity.getClass().getSimpleName() + ", matcher=" + getMatcher() + ", qty=" + _quantity);
            ItemStack extracted = ((TileEntityAutoBlockInventory) entity).extractItems(getMatcher(), _quantity);
            getEntity().setCurrentItemOrArmor(0, extracted);
            //debugLog(LOGGER, "Extracted: " + extracted);
        }

        return UpdateResult.DONE;
    }

    @Override
    public void debugInfo(StringBuilder builder)
    {
        super.debugInfo(builder);
        builder.append(", quantity=").append(_quantity);
    }

    private final int _quantity;
    private BlockPos _workArea;

    private static final Logger LOGGER = LogManager.getLogger();
}
