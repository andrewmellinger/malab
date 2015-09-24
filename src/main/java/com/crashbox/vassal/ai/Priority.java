package com.crashbox.vassal.ai;

import com.crashbox.vassal.VassalUtils;
import com.crashbox.vassal.task.ITask;
import com.crashbox.vassal.task.TaskBase;
import com.crashbox.vassal.task.TaskPair;
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
            else
            {
                LOGGER.debug("Not computing because unresolved: " + task);
            }
        }

        return bestTask;
    }

    public static int computeDistanceCost(BlockPos startPos, BlockPos endPos, double speed)
    {
        return (int) (( Math.sqrt(startPos.distanceSq(endPos)) / speed ) / 40);
    }

    public static int computeDistanceCost(BlockPos startPos, BlockPos endPos)
    {
        return ( (int) Math.sqrt(startPos.distanceSq(endPos)) ) / 10;
    }

    private static final Logger LOGGER = LogManager.getLogger();
}
