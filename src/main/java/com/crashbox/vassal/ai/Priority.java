package com.crashbox.vassal.ai;

import com.crashbox.vassal.task.ITask;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class Priority
{
    private static final int QUARRY_MOVE_QUARRY_BLOCK_VALUE = 20;

    private static int QUARRY_IDLE_HARVESTING_VALUE = 0;
    private static int FORESTER_IDLE_HARVESTING_VALUE = -5;
    private static int GENERIC_CLEAN_UP_TASK_VALUE = 0;
    private static int QUARRY_DEPTH_BLOCKS_PER_PLUS = 10;


    private static int STAIR_BUILDER_VALUE = 10;

    private static int FORESTER_PICKUP_SAPLING_VALUE = 10;
    private static int FORESTER_PLANT_SAPLING_VALUE = 10;
    private static int FORESTER_HARVEST_VALUE = 0;

    private static int FORESTER_STORAGE_SAPLING_PLANT_VALUE = 20;
    private static int CHEST_STORAGE_AVAIL_VALUE = 0;

    private static int QUARRY_ITEM_HARVEST_VALUE = 5;            // Get it from a quarry before chest
    private static int CHEST_ITEM_AVAIL_VALUE = 0;

    private static int WORKBENCH_INVENTORY_OUT_REQUEST_VALUE = 40;
    private static int WORKBENCH_INVENTORY_LOW_REQUEST_VALUE = 20;

    private static int WORKBENCH_ITEM_REQUEST_VALUE = 0;
    private static int WORKBENCH_STORAGE_AVAIL_VALUE = 0;

    private static int FURNACE_ASK_THRESHOLD = 8;
    private static int FURNACE_FUEL_PIECES_PER_PLUS = 5;

    public static void addGameRules(World world)
    {
        GameRules rules = world.getGameRules();
        rules.addGameRule("vassal.distance", "64", GameRules.ValueType.NUMERICAL_VALUE);
        rules.addGameRule("vassal.quarry.idle.harvesting.priority", "0", GameRules.ValueType.NUMERICAL_VALUE);
        rules.addGameRule("vassal.forester.idle.harvesting.priority", "-5", GameRules.ValueType.NUMERICAL_VALUE);
    }

    public static int getLongestDistance(World world)
    {
        return world.getGameRules().getInt("vassal.quarry.idle.harvesting.priority");
    }

    public static int getQuarryIdleHarvestingValue(World world)
    {
        return world.getGameRules().getInt("vassal.quarry.idle.harvesting.priority");
    }

    public static int getForesterIdleHarvestingValue(World world)
    {
        return world.getGameRules().getInt("vassal.forester.idle.harvesting.priority");
    }




    /** Priority for moving the quarry block. */
    public static int getQuarryMoveQuarryBlockValue()
    {
        return QUARRY_MOVE_QUARRY_BLOCK_VALUE;
    }

    public static int getStairBuilderValue()
    {
        return STAIR_BUILDER_VALUE;
    }


    public static int getForesterPickupSaplingValue()
    {
        return FORESTER_PICKUP_SAPLING_VALUE;
    }

    public static int getForesterPlantSaplingValue()
    {
        return FORESTER_PLANT_SAPLING_VALUE;
    }

    public static int getForesterHarvestValue()
    {
        return FORESTER_HARVEST_VALUE;
    }

    public static int getGenericCleanUpTaskValue()
    {
        return GENERIC_CLEAN_UP_TASK_VALUE;
    }

    public static int getForesterStorageSaplingPlantValue()
    {
        return FORESTER_STORAGE_SAPLING_PLANT_VALUE;
    }

    public static int getChestStorageAvailValue()
    {
        return CHEST_STORAGE_AVAIL_VALUE;
    }

    public static int getQuarryItemHarvestValue()
    {
        return QUARRY_ITEM_HARVEST_VALUE;
    }

    public static int getChestItemAvailValue()
    {
        return CHEST_ITEM_AVAIL_VALUE;
    }

    public static int getWorkbenchInventoryOutRequestValue()
    {
        return WORKBENCH_INVENTORY_OUT_REQUEST_VALUE;
    }

    public static int getWorkbenchInventoryLowRequestValue()
    {
        return WORKBENCH_INVENTORY_LOW_REQUEST_VALUE;
    }

    public static int getWorkbenchItemRequestValue()
    {
        return WORKBENCH_ITEM_REQUEST_VALUE;
    }

    public static int getWorkbenchStorageAvailValue()
    {
        return WORKBENCH_STORAGE_AVAIL_VALUE;
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
        }

        return bestTask;
    }

    /**
     * How much value mining at a particular depth provides.  The deeper the more depth.
     * @param y Current y value.
     * @return Value to mine at that depth.
     */
    public static int quarryDepthValue(int y)
    {
        if (y < 60)
            return (60 - y)/QUARRY_DEPTH_BLOCKS_PER_PLUS;

        return 0;
    }

    public static int getFuelNeed(ItemStack fuelStack)
    {
        if (fuelStack == null)
            return 100;

        int current = fuelStack.stackSize;
        if (current > FURNACE_ASK_THRESHOLD)
            return 0;


        return (FURNACE_ASK_THRESHOLD - current)/FURNACE_FUEL_PIECES_PER_PLUS;
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

    public static int computeDistanceCost(World world, BlockPos endPos, double speed, BlockPos startPos)
    {
        // 1 point for every 5 blocks for a normal zombie.  In the future we
        // will compute seconds.
        // 5 / 1.24 -> 4 / 4 = 1
        // 54 / 1.25 -> 43.2 /4 = 10.8
//        return (int) (( Math.sqrt(startPos.distanceSq(endPos)) / speed ) / 4D);

        // We don't want to do this.
        int longestDistance = getLongestDistance(world);
        if (startPos.distanceSq(endPos) > ( longestDistance * longestDistance))
            return Integer.MAX_VALUE;

        return (int) (( Math.sqrt(startPos.distanceSq(endPos)) / speed ) / 2D);
    }

    private static final Logger LOGGER = LogManager.getLogger();
}
