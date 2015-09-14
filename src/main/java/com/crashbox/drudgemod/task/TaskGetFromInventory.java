package com.crashbox.drudgemod.task;

import com.crashbox.drudgemod.ai.EntityAIDrudge;
import com.crashbox.drudgemod.beacon.TileEntityBeaconInventory;
import com.crashbox.drudgemod.common.ItemStackMatcher;
import com.crashbox.drudgemod.messaging.TRGetFromInventory;
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
    public TaskGetFromInventory(EntityAIDrudge performer, TRGetFromInventory message)
    {
        super(performer, message.getSender(), message.getValue(), message.getMatcher());

        int quantity = message.getQuantity();
        if (quantity > getPerformer().getEntity().getCarryCapacity())
            quantity = getPerformer().getEntity().getCarryCapacity();
        _quantity = quantity;

        _workArea = getRequester().getPos();
    }

    @Override
    public BlockPos chooseWorkArea(List<BlockPos> others)
    {
        // We only do this once.
        return _workArea;
    }

    @Override
    public boolean executeAndIsDone()
    {
        _workArea = null;

        // Get the tile entity and extract it
        debugLog(LOGGER, "Going to extract items. ");
        TileEntity entity = getWorld().getTileEntity(getRequester().getPos());
        if (entity instanceof TileEntityBeaconInventory)
        {
            debugLog(LOGGER, "Extracting from TileEntityBeaconInventory ");
            ItemStack extracted = ((TileEntityBeaconInventory) entity).extractItems(_matcher, _quantity);
            getEntity().setCurrentItemOrArmor(0, extracted);
            debugLog(LOGGER, "Extracted: " + extracted);
        }

        return true;
    }

    @Override
    public int getValue()
    {
        return 0;
    }

    @Override
    public void debugInfo(StringBuilder builder)
    {
        super.debugInfo(builder);
        builder.append(", matcher=").append(_matcher);
        builder.append(", quantity=").append(_quantity);
    }

    private final int _quantity;
    private BlockPos _workArea;

    private static final Logger LOGGER = LogManager.getLogger();
}
