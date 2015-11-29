package com.crashbox.malab.task;

import com.crashbox.malab.ai.EntityAIWorkDroid;
import com.crashbox.malab.messaging.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class TaskFactory
{
    public TaskAcquireBase makeTaskFromMessage(EntityAIWorkDroid performer, TRAcquireBase message)
    {
        return (TaskAcquireBase) TaskBase.createTask(performer, message);
//        Class<? extends TaskBase> taskClass = message.getTaskClass();
//        if (taskClass == TaskHarvestTree.class && message instanceof TRHarvest)
//            return new TaskHarvestTree(performer, (TRHarvest)message);
//
//        if (taskClass == TaskQuarry.class && message instanceof TRHarvest)
//            return new TaskQuarry(performer, (TRHarvest)message);
//
//        if (taskClass == TaskPickup.class && message instanceof TRPickup)
//            return new TaskPickup(performer, (TRPickup)message);
//
//        if (taskClass == TaskGetFromInventory.class && message instanceof TRGetFromInventory)
//            return new TaskGetFromInventory(performer, (TRGetFromInventory)message);
//
//        if (taskClass == TaskHarvestBlock.class && message instanceof TRHarvestBlock)
//            return new TaskHarvestBlock(performer, (TRHarvestBlock)message);
//
//        LOGGER.error("Failed to construct task for " + message);
//        return null;
    }

    public TaskDeliverBase makeTaskFromMessage(EntityAIWorkDroid performer, TRDeliverBase message)
    {
        return (TaskDeliverBase) TaskBase.createTask(performer, message);

//        Class<? extends TaskBase> taskClass = message.getTaskClass();
//        if (taskClass == TaskPutInInventory.class && message instanceof TRPutInInventory)
//            return new TaskPutInInventory(performer, (TRPutInInventory)message);
//
//        if (taskClass == TaskPlantSapling.class && message instanceof TRPlantSapling)
//            return new TaskPlantSapling(performer, (TRPlantSapling)message);
//
//        if (taskClass == TaskMakeBigStair.class && message instanceof TRMakeBigStair)
//            return new TaskMakeBigStair(performer, (TRMakeBigStair) message);
//
//        if (taskClass == TaskPlaceBlock.class && message instanceof TRPlaceBlock)
//            return new TaskPlaceBlock(performer, (TRPlaceBlock)message);
//
//        LOGGER.error("Failed to construct task for " + message);
//        return null;
    }


    private static final Logger LOGGER = LogManager.getLogger();
}
