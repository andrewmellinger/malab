package com.crashbox.drudgemod.ai;

import net.minecraft.util.BlockPos;

/**
 * Copyright 2015 Andrew O. Mellinger
 *
 * In this task we take the item(s) in our inventory to the target destination.
 */
public class TaskCarryTo extends TaskBase
{
    /**
     * Create a new carry task.
     * @param tasker Who made the task.
     * @param pos Destination
     */
    public TaskCarryTo(TaskMaster tasker, BlockPos pos)
    {
        super(tasker, pos);
    }

    @Override
    public void execute()
    {
        // All we do for now is move to the target
        getEntity().getNavigator().tryMoveToXYZ(_focusBlock.getX(), _focusBlock.getY(), _focusBlock.getZ(), getEntity().getSpeed() );
    }

    @Override
    public boolean continueExecution()
    {
        // We are continuing as long as we have a path.
        return !getEntity().getNavigator().noPath();
    }
}
