package com.crashbox.drudgemod.task;

import com.crashbox.drudgemod.ai.EntityAIDrudge;
import com.crashbox.drudgemod.messaging.MessageDeliverRequest;
import com.crashbox.drudgemod.messaging.MessageHarvestRequest;
import com.crashbox.drudgemod.messaging.MessagePlantSaplings;
import com.crashbox.drudgemod.messaging.MessageTaskRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class TaskFactory
{
    public TaskBase makeTaskFromMessage(EntityAIDrudge performer, MessageTaskRequest message)
    {
        Class<? extends TaskBase> taskClass = message.getTaskClass();
        if (taskClass == TaskHarvest.class && message instanceof MessageHarvestRequest)
            return new TaskHarvest(performer, (MessageHarvestRequest)message);

        else if (taskClass == TaskDeliver.class && message instanceof MessageDeliverRequest)
            return new TaskDeliver(performer, (MessageDeliverRequest)message);

        else if (taskClass == TaskPlantSapling.class && message instanceof MessagePlantSaplings)
            return new TaskPlantSapling(performer, (MessagePlantSaplings)message);

        else if (taskClass == TaskDeliver.class && message instanceof  MessageDeliverRequest)
            return new TaskDeliver(performer, (MessageDeliverRequest)message);





        LOGGER.error("Failed to construct task for " + message);
        return null;
    }

    private static final Logger LOGGER = LogManager.getLogger();
}
