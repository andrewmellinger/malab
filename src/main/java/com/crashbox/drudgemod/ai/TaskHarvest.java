package com.crashbox.drudgemod.ai;

import com.crashbox.drudgemod.DrudgeUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Queue;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class TaskHarvest extends TaskBase
{
    /**
     * Create a new carry task.
     *  @param tasker   Who made the task.
     * @param center  Block to harvest.
     * @param priority Priority of the task.
     * @param sample
     */
    public TaskHarvest(TaskMaster tasker, BlockPos center, int priority, int radius, int quantity, ItemStack sample)
    {
        super(tasker, center, priority);
        _radius = radius;
        _quantity = quantity;
        _sample = sample;
    }

    @Override
    public void execute()
    {
        // All we do for now is move near the center block then find material
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

        // If we had somewhere to go see if we can harvest
        if (_targetBlock != null)
        {
            if ( getEntity().getPosition().distanceSq(_targetBlock) < 4.2)
            {
                harvestBlock();
                if (getEntity().getHeldItem().stackSize >= 4)
                {
                    complete();
                    return false;
                }
            }
            else
            {
                // If we didn't get close enough bail
                LOGGER.debug("NOT Breaking at at: " + _focusBlock);
            }

            // Quantity check
        }

        if (_harvestList != null)
        {
            _harvestBlock = _harvestList.poll();
        }

        if (_harvestBlock == null)
        {
            // Find blocks in a tree
            _harvestList = RingedSearcher.findTree(getEntity().getEntityWorld(), _focusBlock, _radius, 10, _sample );
            if (_harvestList == null)
            {
                // Didn't find any blocks anywhere
                complete();
                return false;
            }

            _harvestBlock = _harvestList.poll();
            if (_harvestBlock == null)
            {
                // Didn't find any blocks anywhere
                complete();
                return false;
            }
        }

        // At this point we have a harvest block, set a block to walk to
        _targetBlock = new BlockPos(_harvestBlock.getX(), _focusBlock.getY(), _harvestBlock.getZ());

        getEntity().getNavigator()
                .tryMoveToXYZ(_targetBlock.getX(), _targetBlock.getY(), _targetBlock.getZ(), getEntity().getSpeed());
        return true;
    }


    private void harvestBlock()
    {
        if (getEntity().getHeldItem() == null)
        {
            IBlockState blockState = getEntity().getEntityWorld().getBlockState(_harvestBlock);
            Block blockType = blockState.getBlock();

            int meta = blockType.getMetaFromState(blockState);
            ItemStack newStack = new ItemStack(blockType, 1, meta);

            getEntity().getEntityWorld().destroyBlock(_harvestBlock, false);
            getEntity().setCurrentItemOrArmor(0, newStack);
        }
        else
        {
            ItemStack held = getEntity().getHeldItem();
            if (!DrudgeUtils.harvestInto(getEntity().getEntityWorld(), _harvestBlock, held))
            {
                LOGGER.debug("Failed to harvest block intom inventory");
            }


        }
    }

    @Override
    public String toString()
    {
        return "TaskHarvest{}";
    }

    private final int _radius;
    private final int _quantity;
    private final ItemStack _sample;
    private Queue<BlockPos> _harvestList;
    private BlockPos _harvestBlock;
    private BlockPos _targetBlock;

    private static final Logger LOGGER = LogManager.getLogger();

}
