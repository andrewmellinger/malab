package com.crashbox.drudgemod.ai;

import com.crashbox.drudgemod.DrudgeUtils;
import com.crashbox.drudgemod.task.TaskBase;
import com.crashbox.drudgemod.task.TaskPair;
import net.minecraft.util.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class Priority
{
    /**
     * From a list of task pairs, select the best one based on this position.
     * @param pos The current position.
     * @param tasks The list of tasks.
     * @param speed How fast the entity moves.
     * @return The best task.
     */
    public static TaskPair selectBestTaskPair(BlockPos pos, List<TaskPair> tasks, double speed)
    {
        int bestValue = Integer.MIN_VALUE;
        TaskPair bestTask = null;

        if (tasks == null || tasks.isEmpty())
            return null;

        for (TaskPair pair : tasks)
        {
            // If unresolved (has pre-reqs) then skip it
            if (pair.getResolving() == TaskPair.Resolving.RESOLVED)
            {
                int value = Priority.getTaskValue(pos, pair, speed);
                if (value > bestValue)
                {
                    bestValue = value;
                    bestTask = pair;
                }
            }
            else
            {
                LOGGER.debug("Not computing because unresolved: " + pair);
            }

        }

        return bestTask;
    }

    public static int getTaskValue(BlockPos pos, TaskPair pair, double speed)
    {
        int value = 0;

        // Output is pos -> pos (cost) -> pos (cost) -> pos (cost) -> (total)
        String str = DrudgeUtils.getSimpleName(pair.getAcquireTask()) + "." +
                DrudgeUtils.getSimpleName(pair.getDeliverTask()) + " = " +
                pos.toString() + " -> ";

        for (TaskBase task : pair.asList())
        {
            if (task != null)
            {
                int cost = Priority.computeDistanceCost(pos, task.getCoarsePos(), speed);
                int val = task.getValue();
                value = value - cost + val;
                pos = task.getCoarsePos();
                str += pos.toString() + "(" + cost + "," + val + ") -> ";
            }
        }

        str += " total:" + value;
        LOGGER.debug(str);
        return value;
    }

    public static int computeDistanceCost(BlockPos startPos, BlockPos endPos, double speed)
    {
        return (int) (( Math.sqrt(startPos.distanceSq(endPos)) / speed ) / 20);
    }

    public static int computeDistanceCost(BlockPos startPos, BlockPos endPos)
    {
        return ( (int) Math.sqrt(startPos.distanceSq(endPos)) ) / 10;
    }

    private static final Logger LOGGER = LogManager.getLogger();
}
