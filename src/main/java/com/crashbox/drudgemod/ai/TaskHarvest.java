package com.crashbox.drudgemod.ai;

import com.crashbox.drudgemod.tasker.TileEntityTaskerInventory;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
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
     *
     * @param tasker   Who made the task.
     * @param harvest  Block to harvest.
     * @param priority Priority of the task.
     */
    public TaskHarvest(TaskMaster tasker, BlockPos harvest, int priority)
    {
        super(tasker, harvest, priority);
    }

    @Override
    public void execute()
    {
        // All we do for now is move to the target
        getEntity().getNavigator()
                .tryMoveToXYZ(_focusBlock.getX(), _focusBlock.getY(), _focusBlock.getZ(), getEntity().getSpeed());
    }

    @Override
    public boolean continueExecution()
    {
        // We are continuing as long as we have a path.
        if (!getEntity().getNavigator().noPath())
        {
            return true;
        }

        // If we made it, break the block
        double dist = getEntity().getPosition().distanceSq(_focusBlock);
        if (dist < 4.2)
        {
            LOGGER.debug("Breaking at at: " + _focusBlock + ", distance: " + dist);

            Block blockType = getEntity().getEntityWorld().getBlockState(_focusBlock).getBlock();
            Item itemType = Item.getItemFromBlock(blockType);
            getEntity().getEntityWorld().destroyBlock(_focusBlock, false);
            getEntity().setCurrentItemOrArmor(0, new ItemStack(itemType));
        }
        else
        {
            // If we didn't get close enough bail
            LOGGER.debug("NOT Breaking at at: " + _focusBlock + ", distance: " + dist);
        }
        complete();
        return false;
    }

    @Override
    public String toString()
    {
        return "TaskHarvest{}";
    }

    private static final Logger LOGGER = LogManager.getLogger();

}
