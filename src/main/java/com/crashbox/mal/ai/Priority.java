package com.crashbox.mal.ai;

import com.crashbox.mal.MALMain;
import com.crashbox.mal.task.ITask;
import com.crashbox.mal.util.MALUtils;
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
    // Delays between responses from auto blocks
    public static final int AVAILABILITY_RESPONSE_DELAY_MS = 600;

    private static final int QUARRY_MOVE_QUARRY_BLOCK_VALUE = 20;

    private static final String QUARRY_HARVEST_PRIORITY = "mal.quarry.harvest.value";
    private static final String QUARRY_IDLE_HARVEST_PRIORITY = "mal.quarry.idle.harvest.value";

    //----------------------------------------------------------------------------------------------

    @Deprecated
    public static void setupGameRules(World world)
    {
        GameRules rules = world.getGameRules();

        if (!rules.hasRule(QUARRY_HARVEST_PRIORITY))
            rules.addGameRule(QUARRY_HARVEST_PRIORITY, "5", GameRules.ValueType.NUMERICAL_VALUE);

        if (!rules.hasRule(QUARRY_IDLE_HARVEST_PRIORITY))
            rules.addGameRule(QUARRY_IDLE_HARVEST_PRIORITY, "0", GameRules.ValueType.NUMERICAL_VALUE);
    }

    public static int getQuarryItemHarvestValue(World world)
    {
        return world.getGameRules().getInt(QUARRY_HARVEST_PRIORITY);
    }

    public static int getQuarryIdleHarvestValue(World world)
    {
        return world.getGameRules().getInt(QUARRY_IDLE_HARVEST_PRIORITY);
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

    public static int getQuarryCleanTopValue()
    {
        return 20;
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
            return (int) ((60 - y) * MALMain.CONFIG.getQuarryDepthCoefficientValue());

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

        int value = (int) (( Math.sqrt(startPos.distanceSq(endPos)) / speed ) / 2D);

        // Now, add in Y if the specified it
        value += (int) (Math.abs(startPos.getY() - endPos.getY()) *
                MALMain.CONFIG.getDistanceYCoefficient());

        return value;
    }

    public static boolean outOfRange(World world, BlockPos startPos, BlockPos endPos)
    {
        int longestDistance = MALMain.CONFIG.getMaxXZDistance();
        return MALUtils.sqDistXZ(startPos, endPos) > (longestDistance * longestDistance) ||
                (Math.abs(startPos.getY() - endPos.getY()) > MALMain.CONFIG.getMaxYDistance());

    }

}
