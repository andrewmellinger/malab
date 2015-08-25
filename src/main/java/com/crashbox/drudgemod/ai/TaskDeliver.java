package com.crashbox.drudgemod.ai;

import com.crashbox.drudgemod.tasker.TileEntityTaskerInventory;
import net.minecraft.item.ItemStack;

/**
 * Copyright 2015 Andrew O. Mellinger
 * <p/>
 * In this task we take the item(s) in our inventory to the target destination.
 */
public class TaskDeliver extends TaskBase
{
    /**
     * Create a new carry task.
     *  @param tasker    Who made the task.
     * @param recipient Target of the delivery.
     * @param itemSample  Then item to deliver.
     * @param slot      Where to place the items.
     * @param quantity How much they can handle
     */
    public TaskDeliver(TaskMaster tasker, TileEntityTaskerInventory recipient, ItemStack itemSample, int slot, int quantity)
    {
        super(tasker, recipient.getPos(), 0);
        _recipient = recipient;
        _itemSample = itemSample;
        _slot = slot;
        _quantity = quantity;
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
        // All we do for now is move to the target
        getEntity().getNavigator()
                .tryMoveToXYZ(_focusBlock.getX(), _focusBlock.getY(), _focusBlock.getZ(), getEntity().getSpeed());
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
        double dist = getEntity().getPosition().distanceSq(_recipient.getPos());
        if (dist < 4.2)
        {
            ItemStack current = getEntity().getHeldItem();
            current = _recipient.mergeIntoSlot(current, _slot);
            getEntity().setCurrentItemOrArmor(0, current);
        }
        complete();
    }

    @Override
    public String toString()
    {
        return "TaskCarryTo{" +
                "_recipient=" + Integer.toHexString(_recipient.hashCode()) +
                ", _itemSample=" + _itemSample +
                ", _slot=" + _slot +
                ", _quantity=" + _quantity +
                '}';
    }

    private final TileEntityTaskerInventory _recipient;
    private final ItemStack _itemSample;
    private final int _slot;
    private final int _quantity;
}
