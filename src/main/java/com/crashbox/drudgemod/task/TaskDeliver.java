package com.crashbox.drudgemod.task;

import com.crashbox.drudgemod.ai.EntityAIDrudge;
import com.crashbox.drudgemod.beacon.TileEntityBeaconInventory;
import com.crashbox.drudgemod.messaging.IMessager;
import com.crashbox.drudgemod.messaging.Message;
import com.crashbox.drudgemod.messaging.MessageDeliverRequest;
import com.crashbox.drudgemod.messaging.MessageItemRequest;
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
public class TaskDeliver extends TaskBase
{
    public TaskDeliver(EntityAIDrudge performer, MessageDeliverRequest message)
    {
        super(performer, message.getSender(), message.getPriority());
        _itemSample = message.getItemSample();
        _quantity = message.getQuantity();
        _slot = message.getSlot();
    }

    public ItemStack getItemSample()
    {
        return _itemSample;
    }

    public int getQuantity()
    {
        return _quantity;
    }

    @Override
    public void execute()
    {
        tryMoveTo(getRequester().getPos());
    }

    @Override
    public void resetTask()
    {
        execute();
    }

    @Override
    public void updateTask()
    {
        // We are continuing as long as we have a path.
        if (!getEntity().getNavigator().noPath())
        {
            return;
        }

        // Now we should be at the end
        // If we made it, place in the inventory
        double dist = getEntity().getPosition().distanceSq(getRequester().getPos());
        if (dist < 4.2)
        {
            ItemStack current = getEntity().getHeldItem();

            IMessager requester = getRequester();
            if (requester instanceof TileEntityBeaconInventory)
                current = ((TileEntityBeaconInventory) requester).mergeIntoSlot(current, _slot);
            else
                LOGGER.warn("Could not deliver item to target. It isn't an inventory: " + getRequester());
            getEntity().setCurrentItemOrArmor(0, current);
        }
        complete();
    }

    @Override
    public Message resolve()
    {
        // If the drudge already has the desired item, we are a go.
        if (getEntity().getHeldItem() != null && getEntity().getHeldItem().isItemEqual(_itemSample))
        {
            setResolving(Resolving.RESOLVED);
            return null;
        }

        // Return a message showing what we need.
        setResolving(Resolving.RESOLVING);
        return new MessageItemRequest(getPerformer(), null, _itemSample, _quantity);
    }

    @Override
    public int getValue()
    {
        // No additional cost beyond delivery
        return _priority;
    }


    @Override
    public BlockPos selectWorkArea(List<BlockPos> others)
    {
        // We just work here.
        return getRequester().getPos();
    }

    @Override
    public String toString()
    {
        return "TaskCarryTo{" +
                "_requester=" + Integer.toHexString(getRequester().hashCode()) +
                ", _itemSample=" + _itemSample +
                ", _slot=" + _slot +
                ", _quantity=" + _quantity +
                '}';
    }

    private final ItemStack _itemSample;
    private final int _slot;
    private final int _quantity;

    private static final Logger LOGGER = LogManager.getLogger();
}
