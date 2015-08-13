package com.crashbox.drudgemod.ai;

import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class TaskHarvest extends TaskBase
{
    /**
     * Create a new carry task.
     * @param tasker Who made the task.
     * @param pos Destination
     */
    public TaskHarvest(TaskMaster tasker, BlockPos pos)
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
        if (getEntity().getNavigator().noPath())
        {
            // If we made it, break the block
            double dist = getEntity().getPosition().distanceSq(_focusBlock);
            if (dist < 4.2)
            {
                LOGGER.debug("dist: " + dist);

                Block blockType = getEntity().getEntityWorld().getBlockState(_focusBlock).getBlock();
                getEntity().setCurrentItemOrArmor(0, new ItemStack(Items.iron_sword));
                getEntity().getEntityWorld().destroyBlock(_focusBlock, true);
            }

            complete();
            return false;
        }
        return true;
    }

    private static final Logger LOGGER = LogManager.getLogger();

}
