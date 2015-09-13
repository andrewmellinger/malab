package com.crashbox.drudgemod.task;

import com.crashbox.drudgemod.ai.AIUtils;
import com.crashbox.drudgemod.ai.EntityAIDrudge;
import com.crashbox.drudgemod.messaging.MessagePickupRequest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;

import java.util.List;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class TaskPickup extends TaskAcquireBase
{
    public TaskPickup(EntityAIDrudge performer, MessagePickupRequest message)
    {
        super(performer, message.getSender(), message.getValue());
        _item = message.getItem();
    }

    @Override
    public BlockPos chooseWorkArea(List<BlockPos> others)
    {
        EntityItem eItem = AIUtils.findFirstEntityOfTypeOnGround(getEntity().getEntityWorld(), getRequester().getPos(),
                getRequester().getRadius(), _item);

        if (eItem != null)
            return eItem.getPosition();

        return null;
    }

    @Override
    public boolean executeAndIsDone()
    {
        EntityItem eItem = AIUtils.findFirstEntityOfTypeOnGround(getEntity().getEntityWorld(), getEntity()
                .getPosition(), 2, _item);

        if (eItem == null)
            return true;

        ItemStack collected = AIUtils.collectEntityIntoNewStack(getWorld(), eItem.getPosition(), 2, _item);
        getEntity().setCurrentItemOrArmor(0, collected);

        return true;
    }

    @Override
    public int getValue()
    {
        return 0;
    }

    //=============================================================================================

    private final Item _item;
}
