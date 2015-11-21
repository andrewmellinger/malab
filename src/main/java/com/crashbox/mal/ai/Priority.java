package com.crashbox.mal.ai;

import com.crashbox.mal.task.ITask;
import com.crashbox.mal.util.VassalUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

import java.util.List;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class Priority
{
    // Delays between responses from beacon
    public static final int BEACON_AVAILABILITY_RESPONSE_DELAY_MS = 600;


    private static final int QUARRY_MOVE_QUARRY_BLOCK_VALUE = 20;

    private static final String MAX_DISTANCE = "vassal.distance";

    private static final String FORESTER_HARVEST_PRIORITY = "vassal.forester.harvest.value";
    private static final String FORESTER_IDLE_HARVEST_PRIORITY = "vassal.forester.idle.harvest.value";

    private static final String QUARRY_HARVEST_PRIORITY = "vassal.quarry.harvest.value";
    private static final String QUARRY_IDLE_HARVEST_PRIORITY = "vassal.quarry.idle.harvest.value";

    private static final String QUARRY_DEPTH_PRIORITY_PER_BLOCK = "vassal.quarry.depth.value.tenths.per.block";

    public static void setupGameRules(World world)
    {
        GameRules rules = world.getGameRules();

        if (!rules.hasRule(MAX_DISTANCE))
            rules.addGameRule(MAX_DISTANCE, "64", GameRules.ValueType.NUMERICAL_VALUE);

        if (!rules.hasRule(FORESTER_HARVEST_PRIORITY))
            rules.addGameRule(FORESTER_HARVEST_PRIORITY, "0", GameRules.ValueType.NUMERICAL_VALUE);

        if (!rules.hasRule(FORESTER_IDLE_HARVEST_PRIORITY))
            rules.addGameRule(FORESTER_IDLE_HARVEST_PRIORITY, "-5", GameRules.ValueType.NUMERICAL_VALUE);

        if (!rules.hasRule(QUARRY_HARVEST_PRIORITY))
            rules.addGameRule(QUARRY_HARVEST_PRIORITY, "5", GameRules.ValueType.NUMERICAL_VALUE);

        if (!rules.hasRule(QUARRY_IDLE_HARVEST_PRIORITY))
            rules.addGameRule(QUARRY_IDLE_HARVEST_PRIORITY, "0", GameRules.ValueType.NUMERICAL_VALUE);

        if (!rules.hasRule(QUARRY_DEPTH_PRIORITY_PER_BLOCK))
            rules.addGameRule(QUARRY_DEPTH_PRIORITY_PER_BLOCK, "2", GameRules.ValueType.NUMERICAL_VALUE);
    }

    public static int getLongestDistance(World world)
    {
        return world.getGameRules().getInt(MAX_DISTANCE);
    }

    public static int getForesterHarvestValue(World world)
    {
        return world.getGameRules().getInt(FORESTER_HARVEST_PRIORITY);
    }

    public static int getForesterIdleHarvestValue(World world)
    {
        return world.getGameRules().getInt(FORESTER_IDLE_HARVEST_PRIORITY);
    }

    public static int getQuarryItemHarvestValue(World world)
    {
        return world.getGameRules().getInt(QUARRY_HARVEST_PRIORITY);
    }

    public static int getQuarryIdleHarvestValue(World world)
    {
        return world.getGameRules().getInt(QUARRY_IDLE_HARVEST_PRIORITY);
    }

    public static int getQuarryDepthValueTenthsPerBlock(World world)
    {
        return world.getGameRules().getInt(QUARRY_DEPTH_PRIORITY_PER_BLOCK);
    }


    /** Priority for moving the quarry block. */
    public static int getQuarryMoveQuarryBlockValue()
    {
        return QUARRY_MOVE_QUARRY_BLOCK_VALUE;
    }

    public static int getStairBuilderValue()
    {
        return 10;
    }

    public static int getForesterPickupSaplingValue()
    {
        return 10;
    }

    public static int getForesterPlantSaplingValue()
    {
        return 10;
    }

    public static int getGenericCleanUpTaskValue()
    {
        return 0;
    }

    public static int getForesterStorageSaplingPlantValue()
    {
        return 20;
    }

    public static int getChestStorageAvailValue()
    {
        return 0;
    }

    public static int getChestItemAvailValue()
    {
        return 0;
    }

    public static int getWorkbenchInventoryOutRequestValue()
    {
        return 40;
    }

    public static int getWorkbenchInventoryLowRequestValue()
    {
        return 20;
    }

    public static int getWorkbenchItemRequestValue()
    {
        return 0;
    }

    public static int getWorkbenchStorageAvailValue()
    {
        return 0;
    }

    /**
     * From a list of task pairs, select the best one based on this position.
     * @param tasks The list of tasks.
     * @param speed How fast the entity moves.
     * @return The best task.
     */
    public static ITask selectBestTask(List<ITask> tasks, double speed)
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

                // If the value is better than what we had AND not total junk, try it out.
                // -100 is arbitrary, mostly it keeps us from going too far.
                if (value > bestValue && value > -100)
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
     *
     * @param world The world we are in.
     * @param y Current y value.
     * @return Value to mine at that depth.
     */
    public static int quarryDepthValue(World world, int y)
    {
        if (y < 60)
            return ((60 - y) * getQuarryDepthValueTenthsPerBlock(world))/10;

        return 0;
    }

    public static int getFuelNeed(ItemStack fuelStack)
    {
        if (fuelStack == null)
            return 100;

        int current = fuelStack.stackSize;
        if (current > 8)
            return 0;


        return (8 - current)/ 5;
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

    public static int computeDistanceCost(BlockPos endPos, double speed, BlockPos startPos)
    {
        // 1 point for every 5 blocks for a normal zombie.  In the future we
        // will compute seconds.
        // 5 / 1.24 -> 4 / 4 = 1
        // 54 / 1.25 -> 43.2 /4 = 10.8
//        return (int) (( Math.sqrt(startPos.distanceSq(endPos)) / speed ) / 4D);

        return (int) (( Math.sqrt(startPos.distanceSq(endPos)) / speed ) / 2D);
    }

    public static boolean outOfRange(World world, BlockPos startPos, BlockPos endPos)
    {
        int longestDistance = getLongestDistance(world);
        return VassalUtils.sqDistXZ(startPos, endPos) > ( longestDistance * longestDistance );
    }

}
