package com.crashbox.drudgemod.messaging;

import com.crashbox.drudgemod.task.TaskPlantSapling;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class TRPlantSapling extends TRDeliverBase
{
    public TRPlantSapling(IMessager sender, IMessager receiver, Object transactionID, int priority)
    {
        super(sender, receiver, transactionID, priority, TaskPlantSapling.class, null, 1);
    }


}
