package com.crashbox.vassal.messaging;

import com.crashbox.vassal.common.ItemTypeMatcher;
import com.crashbox.vassal.task.TaskPlantSapling;
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
