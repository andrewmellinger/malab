package com.crashbox.drudgemod.task;

import com.crashbox.drudgemod.ai.EntityAIDrudge;
import com.crashbox.drudgemod.beacon.TileEntityBeaconInventory;
import com.crashbox.drudgemod.common.ItemStackMatcher;
import com.crashbox.drudgemod.messaging.*;
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
public class TaskStore extends TaskDeliverBase
{
    public TaskStore(EntityAIDrudge performer, MessageStoreRequest message)
    {
        super(performer, message.getSender(), message.getValue());
        _matcher = message.getMatcher();
        _quantity = message.getQuantity();
        _slot = message.getSlot();
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
    public BlockPos chooseWorkArea(List<BlockPos> others)
    {
        // We just work here.
        return getRequester().getPos();
    }

    @Override
    public boolean executeAndIsDone()
    {
        ItemStack current = getEntity().getHeldItem();

        IMessager requester = getRequester();
        if (requester instanceof TileEntityBeaconInventory)
        {
            current = ((TileEntityBeaconInventory) requester).mergeIntoSlot(current, _slot);
            getEntity().setCurrentItemOrArmor(0, current);
        }
        else
        {
            getEntity().dropHeldItem();
            LOGGER.warn("Could not deliver item to target. It isn't an inventory: " + getRequester());
        }

        return true;
    }

    @Override
    public int getValue()
    {
        // No additional cost beyond delivery
        return _priority;
    }


    @Override
    public void debugInfo(StringBuilder builder)
    {
        super.debugInfo(builder);
        builder.append(", itemSample=").append(_matcher);
        builder.append(", slot=").append(_slot);
        builder.append(", quantity=").append(_quantity);
    }

    private final ItemStackMatcher _matcher;
    private final int _slot;
    private final int _quantity;

    private static final Logger LOGGER = LogManager.getLogger();
}
