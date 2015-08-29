package com.crashbox.drudgemod.ai;

import com.crashbox.drudgemod.beacon.BeaconBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class TaskPlantSapling extends TaskBase
{
    public TaskPlantSapling(BeaconBase beacon, BlockPos focusBlock, int priority, int radius)
    {
        super(beacon, focusBlock, priority);
        _radius = radius;
    }

    @Override
    public void execute()
    {
        // Move to the area.
        getEntity().getNavigator()
                .tryMoveToXYZ(_focusBlock.getX(), _focusBlock.getY(), _focusBlock.getZ(), getEntity().getSpeed());
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

    private boolean locateSapling()
    {
        // TODO: Build optimized version
        _currentPickup  = AIUtils.findFirstEntityOfTypeOnGround(getEntity().getEntityWorld(), _focusBlock, _radius,
                Item.getItemFromBlock(Blocks.sapling));

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
        _plantingTarget = AIUtils.findEmptyOrchardSquare(getWorld(), _focusBlock, _radius);
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
        _plantingTarget = AIUtils.findEmptyOrchardSquare(getWorld(), _focusBlock, _radius);
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
                ", _radius=" + _radius +
                '}';
    }

    // State management
    private enum GoingTo { SITE, COLLECTION, PLANTING}
    private GoingTo _goingTo = GoingTo.SITE;

    private EntityItem _currentPickup;
    private BlockPos _plantingTarget;
    private final int _radius;
}
