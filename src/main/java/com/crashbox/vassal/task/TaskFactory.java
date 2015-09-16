package com.crashbox.vassal.task;

import com.crashbox.vassal.ai.EntityAIVassal;
import com.crashbox.vassal.messaging.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class TaskFactory
{
    public TaskAcquireBase makeTaskFromMessage(EntityAIVassal performer, TRAcquireBase message)
    {
        Class<? extends TaskBase> taskClass = message.getTaskClass();
        if (taskClass == TaskHarvest.class && message instanceof TRHarvest)
            return new TaskHarvest(performer, (TRHarvest)message);

        if (taskClass == TaskPickup.class && message instanceof TRPickup)
            return new TaskPickup(performer, (TRPickup)message);

        if (taskClass == TaskGetFromInventory.class && message instanceof TRGetFromInventory)
            return new TaskGetFromInventory(performer, (TRGetFromInventory)message);

        LOGGER.error("Failed to construct task for " + message);
        return null;
    }

    public TaskDeliverBase makeTaskFromMessage(EntityAIVassal performer, TRDeliverBase message)
    {
        Class<? extends TaskBase> taskClass = message.getTaskClass();
        if (taskClass == TaskStore.class && message instanceof TRStore)
            return new TaskStore(performer, (TRStore)message);

        else if (taskClass == TaskPlantSapling.class && message instanceof TRPlantSapling)
            return new TaskPlantSapling(performer, (TRPlantSapling)message);

        LOGGER.error("Failed to construct task for " + message);
        return null;
    }


    private static final Logger LOGGER = LogManager.getLogger();
}
