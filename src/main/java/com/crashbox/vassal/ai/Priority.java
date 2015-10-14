package com.crashbox.vassal.ai;

import com.crashbox.vassal.task.ITask;
import net.minecraft.util.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class Priority
{
    private static int QUARRY_MOVE_QUARRY_BLOCK_VALUE = 20;

    private static int STAIR_BUILDER_VALUE = 10;

    private static int FORESTER_IDLE_HARVESTING_VALUE = -5;
    private static int QUARRY_IDLE_HARVESTING_VALUE = 0;

    private static int FORESTER_PICKUP_SAPLING_VALUE = 10;
    private static int FORESTER_PLANT_SAPLING_VALUE = 10;
    private static int FORESTER_HARVEST_VALUE = 0;

    private static int GENERIC_CLEAN_UP_TASK_VALUE = 0;

    private static int FORESTER_STORAGE_SAPLING_PLANT_VALUE = 20;
    private static int CHEST_STORAGE_AVAIL_VALUE = 0;

    private static int QUARRY_ITEM_HARVEST_VALUE = 5;            // Get it from a quarry before chest
    private static int CHEST_ITEM_AVAIL_VALUE = 0;

    private static int WORKBENCH_INVENTORY_OUT_REQUEST_VALUE = 40;
    private static int WORKBENCH_INVENTORY_LOW_REQUEST_VALUE = 20;

    private static int WORKBENCH_ITEM_REQUEST_VALUE = 0;
    private static int WORKBENCH_STORAGE_AVAIL_VALUE = 0;

    private static int LONGEST_DISTANCE = 64;


    public static int getQuarryMoveQuarryBlockValue()
    {
        return QUARRY_MOVE_QUARRY_BLOCK_VALUE;
    }

    public static void setQuarryMoveQuarryBlockValue(int quarryMoveQuarryBlockValue)
    {
        QUARRY_MOVE_QUARRY_BLOCK_VALUE = quarryMoveQuarryBlockValue;
    }

    public static int getStairBuilderValue()
    {
        return STAIR_BUILDER_VALUE;
    }

    public static void setStairBuilderValue(int stairBuilderValue)
    {
        STAIR_BUILDER_VALUE = stairBuilderValue;
    }

    public static int getForesterIdleHarvestingValue()
    {
        return FORESTER_IDLE_HARVESTING_VALUE;
    }

    public static void setForesterIdleHarvestingValue(int foresterIdleHarvestingValue)
    {
        FORESTER_IDLE_HARVESTING_VALUE = foresterIdleHarvestingValue;
    }

    public static int getQuarryIdleHarvestingValue()
    {
        return QUARRY_IDLE_HARVESTING_VALUE;
    }

    public static void setQuarryIdleHarvestingValue(int quarryIdleHarvestingValue)
    {
        QUARRY_IDLE_HARVESTING_VALUE = quarryIdleHarvestingValue;
    }

    public static int getForesterPickupSaplingValue()
    {
        return FORESTER_PICKUP_SAPLING_VALUE;
    }

    public static void setForesterPickupSaplingValue(int foresterPickupSapling)
    {
        FORESTER_PICKUP_SAPLING_VALUE = foresterPickupSapling;
    }

    public static int getForesterPlantSaplingValue()
    {
        return FORESTER_PLANT_SAPLING_VALUE;
    }

    public static void setForesterPlantSaplingValue(int foresterPlantSaplingValue)
    {
        FORESTER_PLANT_SAPLING_VALUE = foresterPlantSaplingValue;
    }

    public static int getForesterHarvestValue()
    {
        return FORESTER_HARVEST_VALUE;
    }

    public static void setForesterHarvestValue(int foresterHarvestValue)
    {
        FORESTER_HARVEST_VALUE = foresterHarvestValue;
    }

    public static int getGenericCleanUpTaskValue()
    {
        return GENERIC_CLEAN_UP_TASK_VALUE;
    }

    public static void setGenericCleanUpTaskValue(int genericCleanUpTaskValue)
    {
        GENERIC_CLEAN_UP_TASK_VALUE = genericCleanUpTaskValue;
    }

    public static int getForesterStorageSaplingPlantValue()
    {
        return FORESTER_STORAGE_SAPLING_PLANT_VALUE;
    }

    public static void setForesterStorageSaplingPlantValue(int foresterStorageSaplingPlantValue)
    {
        FORESTER_STORAGE_SAPLING_PLANT_VALUE = foresterStorageSaplingPlantValue;
    }

    public static int getChestStorageAvailValue()
    {
        return CHEST_STORAGE_AVAIL_VALUE;
    }

    public static void setChestStorageAvailValue(int chestStorageAvailValue)
    {
        CHEST_STORAGE_AVAIL_VALUE = chestStorageAvailValue;
    }

    public static int getQuarryItemHarvestValue()
    {
        return QUARRY_ITEM_HARVEST_VALUE;
    }

    public static void setQuarryItemHarvestValue(int quarryItemHarvestValue)
    {
        QUARRY_ITEM_HARVEST_VALUE = quarryItemHarvestValue;
    }

    public static int getChestItemAvailValue()
    {
        return CHEST_ITEM_AVAIL_VALUE;
    }

    public static void setChestItemAvailValue(int chestItemAvailValue)
    {
        CHEST_ITEM_AVAIL_VALUE = chestItemAvailValue;
    }

    public static int getWorkbenchInventoryOutRequestValue()
    {
        return WORKBENCH_INVENTORY_OUT_REQUEST_VALUE;
    }

    public static void setWorkbenchInventoryOutRequestValue(int workbenchInventoryOutRequestValue)
    {
        WORKBENCH_INVENTORY_OUT_REQUEST_VALUE = workbenchInventoryOutRequestValue;
    }

    public static int getWorkbenchInventoryLowRequestValue()
    {
        return WORKBENCH_INVENTORY_LOW_REQUEST_VALUE;
    }

    public static void setWorkbenchInventoryLowRequestValue(int workbenchInventoryLowRequestValue)
    {
        WORKBENCH_INVENTORY_LOW_REQUEST_VALUE = workbenchInventoryLowRequestValue;
    }

    public static int getWorkbenchItemRequestValue()
    {
        return WORKBENCH_ITEM_REQUEST_VALUE;
    }

    public static void setWorkbenchItemRequestValue(int workbenchItemRequestValue)
    {
        WORKBENCH_ITEM_REQUEST_VALUE = workbenchItemRequestValue;
    }

    public static int getWorkbenchStorageAvailValue()
    {
        return WORKBENCH_STORAGE_AVAIL_VALUE;
    }

    public static void setWorkbenchStorageAvailValue(int workbenchStorageAvailValue)
    {
        WORKBENCH_STORAGE_AVAIL_VALUE = workbenchStorageAvailValue;
    }

    public static int getLongestDistance()
    {
        return LONGEST_DISTANCE;
    }

    public static void setLongestDistance(int longestDistance)
    {
        LONGEST_DISTANCE = longestDistance;
    }

    /**
     * From a list of task pairs, select the best one based on this position.
     * @param pos The current position.
     * @param tasks The list of tasks.
     * @param speed How fast the entity moves.
     * @return The best task.
     */
    public static ITask selectBestTask(BlockPos pos, List<ITask> tasks, double speed)
    {
        int bestValue = Integer.MIN_VALUE;
        ITask bestTask = null;

        if (tasks == null || tasks.isEmpty())
            return null;

        for (ITask task : tasks)
        {
            // If unresolved (has pre-reqs) then skip it
            if (task.resolve())
            {
                int value = task.getValue(speed);
                if (value > bestValue)
                {
                    bestValue = value;
                    bestTask = task;
                }
            }
//            else
//            {
//                LOGGER.debug("Not computing because unresolved: " + task);
//            }
        }

        return bestTask;
    }

    public static int inventoryPressure(int current, int max)
    {
        if (current < 16)
            return 0;

        // No pressure below 16. Linear ramp after that.
        double remain = current - 16;
        double space = max - 16;

        // Linear ramp for now
        return (int) ((remain/space) * 10);
    }

    public static int computeDistanceCost(BlockPos startPos, BlockPos endPos, double speed)
    {
        // 1 point for every 5 blocks for a normal zombie.  In the future we
        // will compute seconds.
        // 5 / 1.24 -> 4 / 4 = 1
        // 54 / 1.25 -> 43.2 /4 = 10.8
//        return (int) (( Math.sqrt(startPos.distanceSq(endPos)) / speed ) / 4D);

        // We don't want to do this.
        if (startPos.distanceSq(endPos) > ( LONGEST_DISTANCE * LONGEST_DISTANCE))
            return Integer.MAX_VALUE;

        return (int) (( Math.sqrt(startPos.distanceSq(endPos)) / speed ) / 2D);
    }

//    public static int computeDistanceCost(BlockPos startPos, BlockPos endPos)
//    {
//        return ( (int) Math.sqrt(startPos.distanceSq(endPos)) ) / 10;
//    }

    private static final Logger LOGGER = LogManager.getLogger();
}
