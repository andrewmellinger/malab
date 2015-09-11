package com.crashbox.drudgemod.task;

import com.crashbox.drudgemod.ai.EntityAIDrudge;
import com.crashbox.drudgemod.beacon.TileEntityBeaconInventory;
import com.crashbox.drudgemod.common.ItemStackMatcher;
import com.crashbox.drudgemod.messaging.*;
import net.minecraft.entity.item.EntityItem;
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
        setState(State.SUCCESS);
    }

    @Override
    public Message resolve()
    {
        // If the drudge already has the desired item, we are a go.
        if (getEntity().getHeldItem() != null && _matcher.matches(getEntity().getHeldItem()))
        {
            setResolving(Resolving.RESOLVED);
            return null;
        }
        else if (getEntity().getHeldItem() != null)
        {
            // If we have something else dump it.
            // TODO:  Find a chest to put it in
            ItemStack held = getEntity().getHeldItem();
            getEntity().setCurrentItemOrArmor(0, null);
            BlockPos pos = getPerformer().getPos();
                    getEntity().getEntityWorld().spawnEntityInWorld(
                            new EntityItem(getEntity().getEntityWorld(), pos.getX(), pos.getY(), pos.getZ(), held));
        }

        // Return a message showing what we need.
        setResolving(Resolving.RESOLVING);
        return new MessageItemRequest(getPerformer(), null, this, _matcher, _quantity);
    }

    @Override
    public TaskBase linkResponses(List<MessageTaskRequest> responses)
    {
        List<MessageTaskRequest> taskResponses = getAllForTask(responses, this);
        if (taskResponses.size() > 0)
        {
            MessageTaskRequest opt = findBestResponseOption(this, taskResponses);
            TaskBase newTask = EntityAIDrudge.TASK_FACTORY.makeTaskFromMessage(getPerformer(), opt);
            newTask.setNextTask(this);
            return newTask;
        }

        return this;
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
