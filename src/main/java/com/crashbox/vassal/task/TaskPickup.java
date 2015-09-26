package com.crashbox.vassal.task;

import com.crashbox.vassal.VassalUtils;
import com.crashbox.vassal.ai.EntityAIVassal;
import com.crashbox.vassal.messaging.TRPickup;
import com.crashbox.vassal.task.ITask.UpdateResult;
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
    public TaskPickup(EntityAIVassal performer, TRPickup message)
    {
        super(performer, message.getSender(), message.getValue(), message.getMatcher());
        _item = message.getItem();
    }

    @Override
    public BlockPos getWorkTarget(List<BlockPos> others)
    {
        EntityItem eItem = VassalUtils.findFirstEntityOfTypeOnGround(getEntity().getEntityWorld(), getRequester().getPos(),
                getRequester().getRadius(), _item);

        if (eItem != null)
            return eItem.getPosition();

        return null;
    }

    @Override
    public UpdateResult executeAndIsDone()
    {
        EntityItem eItem = VassalUtils.findFirstEntityOfTypeOnGround(getEntity().getEntityWorld(), getEntity()
                .getPosition(), 2, _item);

        if (eItem == null)
            return UpdateResult.DONE;

        ItemStack collected = VassalUtils.collectEntityIntoNewStack(getWorld(), eItem.getPosition(), 3, _item);
        getEntity().setCurrentItemOrArmor(0, collected);

        return UpdateResult.DONE;
    }

    @Override
    public int getValue()
    {
        return 0;
    }

    //=============================================================================================

    private final Item _item;
}
