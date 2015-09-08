package com.crashbox.drudgemod.messaging;

import com.crashbox.drudgemod.DrudgeUtils;
import com.crashbox.drudgemod.task.TaskPlantSapling;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class MessagePlantSaplings extends MessageTaskRequest
{
    public MessagePlantSaplings(IMessager sender, IMessager target, Object transactionID, int priority)
    {
        super(sender, target, transactionID, priority, TaskPlantSapling.class);
    }
}
