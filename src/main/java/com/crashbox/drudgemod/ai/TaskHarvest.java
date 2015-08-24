package com.crashbox.drudgemod.ai;

import com.crashbox.drudgemod.DrudgeUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
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

        // If we are in the process of breaking, do that.
        if (_isBreaking)
        {
            _isBreaking = updateBreak();
            if (!_isBreaking)
            {
                LOGGER.debug("Finished breaking, harvesting.");
                harvestBlock();

                if (getEntity().getHeldItem().stackSize >= 4)
                {
                    LOGGER.debug("Reached capacity.  Done");
                    complete();
                    return false;
                }
                _targetBlock = null;
            }
            else
            {
                return true;
            }
        }

        // If we had somewhere to go see if we can harvest
        if (_targetBlock != null)
        {
            if ( getEntity().getPosition().distanceSq(_targetBlock) < 4.2)
            {
                LOGGER.debug("Start breaking");
                _isBreaking = true;
                _breakingTime = 0;
                _previousBreakProgress = 0;
                return true;
            }
            else
            {
                // If we didn't get close enough bail
                LOGGER.debug("NOT Breaking at at: " + _focusBlock);
            }

            // TODO: Quantity check
        }

        // Find things to harvest
        if (_harvestList != null)
        {
            LOGGER.debug("Getting next harvest block");
            _harvestBlock = _harvestList.poll();
        }

        // Get the next block to harvest
        if (_harvestBlock == null)
        {
            LOGGER.debug("Getting next harvest list");
            // Find blocks in a tree
            _harvestList = RingedSearcher.findTree(getEntity().getEntityWorld(), _focusBlock, _radius, 10, _sample );
            if (_harvestList == null)
            {
                // Didn't find any blocks anywhere
                complete();
                LOGGER.debug("Didn't find any blocks to harvest.  Done.");
                return false;
            }

            // Now that we have a new list, get the next block
            _harvestBlock = _harvestList.poll();
        }

        // At this point we have a harvest block, set a block to walk to
        _targetBlock = new BlockPos(_harvestBlock.getX(), _focusBlock.getY(), _harvestBlock.getZ());
        LOGGER.debug("Made new target block.  Moving tio: " + _targetBlock);

        getEntity().getNavigator()
                .tryMoveToXYZ(_targetBlock.getX(), _targetBlock.getY(), _targetBlock.getZ(), getEntity().getSpeed());

        // TODO:  Can't set the path.  Try to find another.

        return true;
    }


    private boolean updateBreak()
    {
//        if (getEntity().getRNG().nextInt(20) == 0)
//        {
//            getEntity().worldObj.playAuxSFX(1010, this.doorPosition, 0);
//        }

        ++this._breakingTime;
        int i = (int)((float)this._breakingTime / 240.0F * 10.0F);

        if (i != this._previousBreakProgress)
        {
            getEntity().worldObj.sendBlockBreakProgress(getEntity().getEntityId(), _harvestBlock, i);
            this._previousBreakProgress = i;
        }

//        if (this._breakingTime == 240 && getEntity().worldObj.getDifficulty() == EnumDifficulty.HARD)
        if (this._breakingTime == 240)
        {
            return false;
//            getEntity().worldObj.setBlockToAir(this.doorPosition);
//            getEntity().worldObj.playAuxSFX(1012, this.doorPosition, 0);
//            getEntity().worldObj.playAuxSFX(2001, this.doorPosition, Block.getIdFromBlock(this.doorBlock));
        }

        return true;
    }


    private void harvestBlock()
    {
        ItemStack targetStack = getEntity().getHeldItem();
        if (targetStack == null)
        {
            targetStack = _sample.copy();
            targetStack.stackSize = 0;

            IBlockState blockState = getEntity().getEntityWorld().getBlockState(_harvestBlock);
            Block blockType = blockState.getBlock();

            getEntity().setCurrentItemOrArmor(0, targetStack);
        }
        else
        {
            // It changed or won't drop the right thing, bail.
            if (!DrudgeUtils.willDrop(getEntity().getEntityWorld(), _harvestBlock, targetStack))
                return;
        }

        ///// BREAK
        getEntity().getEntityWorld().destroyBlock(_harvestBlock, true);

        ///// PICKUP
        AIUtils.collectEntityIntoStack(getEntity().getEntityWorld(), _harvestBlock, 3, targetStack);
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

    private int _breakingTime;
    private int _previousBreakProgress;
    private boolean _isBreaking;

    private static final Logger LOGGER = LogManager.getLogger();

}
