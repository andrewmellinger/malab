package com.crashbox.drudgemod.task;

import com.crashbox.drudgemod.ai.EntityAIDrudge;
import com.crashbox.drudgemod.messaging.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class TaskFactory
{
    public TaskAcquireBase makeTaskFromMessage(EntityAIDrudge performer, MessageAcquireRequest message)
    {
        Class<? extends TaskBase> taskClass = message.getTaskClass();
        if (taskClass == TaskHarvest.class && message instanceof MessageHarvestRequest)
            return new TaskHarvest(performer, (MessageHarvestRequest)message);

        LOGGER.error("Failed to construct task for " + message);
        return null;
    }

    public TaskDeliverBase makeTaskFromMessage(EntityAIDrudge performer, MessageDeliverRequest message)
    {
        Class<? extends TaskBase> taskClass = message.getTaskClass();
        if (taskClass == TaskStore.class && message instanceof MessageStoreRequest)
            return new TaskStore(performer, (MessageStoreRequest)message);

//        else if (taskClass == TaskPlantSapling.class && message instanceof MessagePlantSaplings)
//            return new TaskPlantSapling(performer, (MessagePlantSaplings)message);

        LOGGER.error("Failed to construct task for " + message);
        return null;
    }


    private static final Logger LOGGER = LogManager.getLogger();
}
