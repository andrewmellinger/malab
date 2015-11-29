package com.crashbox.malab.messaging;

import com.crashbox.malab.common.ItemTypeMatcher;
import com.crashbox.malab.task.TaskPlantSapling;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class TRPlantSapling extends TRDeliverBase
{
    public TRPlantSapling(IMessager sender, IMessager receiver, Object transactionID, int priority)
    {
        super(sender, receiver, transactionID, priority, TaskPlantSapling.class,
                new ItemTypeMatcher(Item.getItemFromBlock(Blocks.sapling)), 1);
    }


}
