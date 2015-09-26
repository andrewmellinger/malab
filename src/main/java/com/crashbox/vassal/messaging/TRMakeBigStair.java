package com.crashbox.vassal.messaging;

import com.crashbox.vassal.common.ItemStackMatcher;
import com.crashbox.vassal.task.TaskDeliverBase;
import com.crashbox.vassal.task.TaskMakeBigStair;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

/**
 * Copyright CMU 2015.
 */
public class TRMakeBigStair extends TRDeliverBase
{
    public TRMakeBigStair(IMessager sender, IMessager receiver, Object transactionID, int value, int quantity)
    {
        super(sender, receiver, transactionID, value, TaskMakeBigStair.class,
                new ItemStackMatcher( Blocks.cobblestone), quantity);
    }
}
