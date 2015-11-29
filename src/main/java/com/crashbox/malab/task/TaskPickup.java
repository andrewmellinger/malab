package com.crashbox.malab.task;

import com.crashbox.malab.util.MALUtils;
import com.crashbox.malab.ai.EntityAIWorkDroid;
import com.crashbox.malab.messaging.TRPickup;
import com.crashbox.malab.task.ITask.UpdateResult;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class TaskPickup extends TaskAcquireBase
{
    public TaskPickup(EntityAIWorkDroid performer, TRPickup message)
    {
        super(performer, message.getSender(), message.getValue(), message.getMatcher());
        _item = message.getItem();
    }

    @Override
    public BlockPos getWorkTarget(List<BlockPos> others)
    {
        EntityItem eItem = MALUtils.findFirstEntityOfTypeOnGround(getEntity().getEntityWorld(),
                getRequester().getBlockPos(), getRequester().getRadius() + 2, _item);

        if (eItem != null)
            return eItem.getPosition();

        return null;
    }

    @Override
    public UpdateResult executeAndIsDone()
    {
        EntityItem eItem = MALUtils.findFirstEntityOfTypeOnGround(getEntity().getEntityWorld(), getEntity()
                .getPosition(), 3, _item);

        if (eItem == null)
            return UpdateResult.DONE;

        ItemStack collected = MALUtils.collectEntityIntoNewStack(getWorld(), eItem.getPosition(), 3, _item);

        // ROBUSTNESS CHECK: If we are holding something different than what we are supposed to pick
        // up, drop it instead of just ignoring it.  This is an error.
        ItemStack held = getEntity().getHeldItem();
        if (held != null && !held.isItemEqual(collected))
        {
            LOGGER.info("Had something unexpected in inventory.  Dropping before pickup." + held);
            getEntity().dropHeldItem();
        }

        getEntity().setCurrentItemOrArmor(0, collected);

        return UpdateResult.DONE;
    }

    //=============================================================================================

    private final Item _item;

    private static final Logger LOGGER = LogManager.getLogger();
}
