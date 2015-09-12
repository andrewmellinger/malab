package com.crashbox.drudgemod.task;

import com.crashbox.drudgemod.ai.AIUtils;
import com.crashbox.drudgemod.ai.EntityAIDrudge;
import com.crashbox.drudgemod.messaging.Message;
import com.crashbox.drudgemod.messaging.MessagePlantSaplings;
import com.crashbox.drudgemod.messaging.MessageStorageRequest;
import com.crashbox.drudgemod.messaging.MessageTaskRequest;
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
        super(performer, message.getSender(), message.getValue());
//        setResolving(Resolving.RESOLVED);
    }

    @Override
    public boolean execute()
    {
        return false;

//        // Move to sapling
//        // Pick up sapling
//        // Wait until we have no path.
//        if (!getEntity().getNavigator().noPath())
//        {
//            return;
//        }
//
//        boolean done = false;
//        switch (_goingTo)
//        {
//            case SITE:
//                done = locateSapling();
//                break;
//            case COLLECTION:
//                done = handleCollection();
//                break;
//            case PLANTING:
//                done = handlePlanting();
//        }
//
//        if (done)
//            setState(State.SUCCESS);
    }

    @Override
    public int getValue()
    {
        // SWAG
        return _priority - 10;
    }

    @Override
    public BlockPos chooseWorkArea(List<BlockPos> others)
    {
//        locateSapling();
//        if (_currentPickup != null)
//            return _currentPickup.getPosition();
//
        return null;
    }

    //===================================

//    private boolean locateSapling()
//    {
//        // TODO: Build optimized version
//        _currentPickup  = AIUtils.findFirstEntityOfTypeOnGround(getEntity().getEntityWorld(), getRequester().getPos(),
//                getRequester().getRadius(), Item.getItemFromBlock(Blocks.sapling));
//
//        if (_currentPickup == null)
//            return true;
//        tryMoveTo(_currentPickup.getPosition());
//
//        _goingTo = GoingTo.COLLECTION;
//        return false;
//    }
//
//    private boolean handleCollection()
//    {
//        // Pick up next entry
//        ItemStack collected = AIUtils.collectEntityIntoNewStack(getWorld(), _currentPickup.getPosition(), 2, Item.getItemFromBlock(Blocks.sapling));
//        _currentPickup = null;
//
//        // If we found something in here, let's grab it, otherwise try again
//        if (collected.stackSize > 0)
//            getEntity().setCurrentItemOrArmor(0, collected);
//        else
//            return locateSapling();
//
//        // Go to next planting spot
//        _plantingTarget = AIUtils.findEmptyOrchardSquare(getWorld(), getRequester().getPos(),
//                getRequester().getRadius());
//        if (_plantingTarget == null)
//            return true;
//
//        tryMoveTo(_plantingTarget);
//        _goingTo = GoingTo.PLANTING;
//        return false;
//    }
//
//    private boolean handlePlanting()
//    {
//        ItemStack held = getEntity().getHeldItem();
//
//        // Plant
//        IBlockState state = Blocks.sapling.getStateFromMeta(held.getMetadata());
//        getWorld().setBlockState(_plantingTarget, state);
//
//        // Get next sapling
//        held.stackSize--;
//        if (held.stackSize <= 0)
//        {
//            getEntity().setCurrentItemOrArmor(0, null);
//            return locateSapling();
//        }
//
//        // Go to next planting spot
//        _plantingTarget = AIUtils.findEmptyOrchardSquare(getWorld(), getRequester().getPos(),
//                getRequester().getRadius());
//        if (_plantingTarget == null)
//            return true;
//
//        tryMoveTo(_plantingTarget);
//        _goingTo = GoingTo.PLANTING;
//        return false;
//    }

    @Override
    public void debugInfo(StringBuilder builder)
    {
        super.debugInfo(builder);
    }


    // State management
    private enum GoingTo { SITE, COLLECTION, PLANTING}
    private GoingTo _goingTo = GoingTo.SITE;

    private EntityItem _currentPickup;
    private BlockPos _plantingTarget;
}
