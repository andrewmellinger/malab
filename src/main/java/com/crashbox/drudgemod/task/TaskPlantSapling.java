package com.crashbox.drudgemod.task;

import com.crashbox.drudgemod.ai.AIUtils;
import com.crashbox.drudgemod.ai.EntityAIDrudge;
import com.crashbox.drudgemod.messaging.Message;
import com.crashbox.drudgemod.messaging.MessagePlantSaplings;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;

import java.util.List;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class TaskPlantSapling extends TaskBase
{
    public TaskPlantSapling(EntityAIDrudge performer, MessagePlantSaplings message)
    {
        super(null, message.getSender(), message.getPriority());
        setResolving(Resolving.RESOLVED);
    }

    @Override
    public void execute()
    {
        // Move to the area.
        tryMoveTo(getRequester().getPos());
    }

    @Override
    public void resetTask()
    {
        // TODO:  What is a reasonable restart?
    }

    @Override
    public void updateTask()
    {
        // Move to sapling
        // Pick up sapling
        // Wait until we have no path.
        if (!getEntity().getNavigator().noPath())
        {
            return;
        }

        boolean done = false;
        switch (_goingTo)
        {
            case SITE:
                done = locateSapling();
                break;
            case COLLECTION:
                done = handleCollection();
                break;
            case PLANTING:
                done = handlePlanting();
        }

        if (done)
            complete();
    }

    @Override
    public Message resolve()
    {
        // TODO:  If we have something in inventory find some place to put it
        // return new MessageStorageRequest()
        return null;
    }

    @Override
    public int getValue()
    {
        // SWAG
        return _priority - 10;
    }

    @Override
    public BlockPos selectWorkArea(List<BlockPos> others)
    {
        locateSapling();
        if (_currentPickup != null)
            return _currentPickup.getPosition();

        return null;
    }

    //===================================

    private boolean locateSapling()
    {
        // TODO: Build optimized version
        _currentPickup  = AIUtils.findFirstEntityOfTypeOnGround(getEntity().getEntityWorld(), getRequester().getPos(),
                getRequester().getRadius(), Item.getItemFromBlock(Blocks.sapling));

        if (_currentPickup == null)
            return true;
        tryMoveTo(_currentPickup.getPosition());

        _goingTo = GoingTo.COLLECTION;
        return false;
    }

    private boolean handleCollection()
    {
        // Pick up next entry
        getEntity().setCurrentItemOrArmor(0, _currentPickup.getEntityItem());
        getEntity().getEntityWorld().removeEntity(_currentPickup);
        _currentPickup = null;

        // Go to next planting spot
        _plantingTarget = AIUtils.findEmptyOrchardSquare(getWorld(), getRequester().getPos(),
                getRequester().getRadius());
        if (_plantingTarget == null)
            return true;

        tryMoveTo(_plantingTarget);
        _goingTo = GoingTo.PLANTING;
        return false;
    }

    private boolean handlePlanting()
    {
        ItemStack held = getEntity().getHeldItem();

        // Plant
        IBlockState state = Blocks.sapling.getStateFromMeta(held.getMetadata());
        getWorld().setBlockState(_plantingTarget, state);

        // Get next sapling
        held.stackSize--;
        if (held.stackSize <= 0)
        {
            getEntity().setCurrentItemOrArmor(0, null);
            return locateSapling();
        }

        // Go to next planting spot
        _plantingTarget = AIUtils.findEmptyOrchardSquare(getWorld(), getRequester().getPos(),
                getRequester().getRadius());
        if (_plantingTarget == null)
            return true;

        tryMoveTo(_plantingTarget);
        _goingTo = GoingTo.PLANTING;
        return false;
    }

    @Override
    public String toString()
    {
        return "TaskPlantSapling{" +
                "_goingTo=" + _goingTo +
                ", _currentPickup=" + _currentPickup +
                ", _plantingTarget=" + _plantingTarget +
                '}';
    }

    // State management
    private enum GoingTo { SITE, COLLECTION, PLANTING}
    private GoingTo _goingTo = GoingTo.SITE;

    private EntityItem _currentPickup;
    private BlockPos _plantingTarget;
}
