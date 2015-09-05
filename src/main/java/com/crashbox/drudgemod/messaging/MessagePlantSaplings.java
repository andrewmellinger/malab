package com.crashbox.drudgemod.messaging;

import com.crashbox.drudgemod.task.TaskBase;
import com.crashbox.drudgemod.task.TaskPlantSapling;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class MessagePlantSaplings extends MessageTaskRequest
{
    public MessagePlantSaplings(IMessager sender, IMessager target, int priority)
    {
        super(sender, target, TaskPlantSapling.class, priority);
    }
}
