package com.crashbox.drudgemod.task;

import com.crashbox.drudgemod.DrudgeUtils;
import com.crashbox.drudgemod.ai.AIUtils;
import com.crashbox.drudgemod.ai.EntityAIDrudge;
import com.crashbox.drudgemod.ai.RingedSearcher;
import com.crashbox.drudgemod.messaging.Message;
import com.crashbox.drudgemod.messaging.MessageHarvestRequest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Queue;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class TaskHarvest extends TaskBase
{
    public TaskHarvest(EntityAIDrudge performer, MessageHarvestRequest message)
    {
        super(performer, message.getSender(), message.getPriority());
        _radius = message.getSender().getRadius();
        _quantity = message.getQuantity();
        _sample = message.getSample();
        setResolving(Resolving.RESOLVED);
    }

    @Override
    public void execute()
    {
        // Go to the sender.
        tryMoveTo(getRequester().getPos());
    }

    @Override
    public void resetTask()
    {
        if (findNextBlock())
        {
            startNavigation();
        }
    }

    @Override
    public void updateTask()
    {
        // Wait until we have no path.
        if (!getEntity().getNavigator().noPath())
        {
            return;
        }

        // If we are in the process of breaking, do that.
        if (_isBreaking)
        {
            if (!handleBreaking())
            {
                setState(State.SUCCESS);
            }
            return;
        }

        // If we have a target block, then let's see if we are close enough to start breaking
        if (_targetBlock != null)
        {
            if ( DrudgeUtils.sqDistanceXY(getEntity().getPosition(), _targetBlock, 4 ))
            {
                startBreaking();
                return;
            }
            else
            {
                // If we didn't get close enough, try finding again
                LOGGER.debug("NOT Breaking at at: " + _targetBlock);
            }
        }

        // Try to identify another block to break.
        if (!findNextBlock())
        {
            if (getEntity().getHeldItem() == null)
                setState(State.FAILED);
            else
                setState(State.SUCCESS);
            return;
        }

        // Navigate to another block
        // TODO:  What if we can't navigate there?
        startNavigation();
    }

    @Override
    public Message resolve()
    {
        // TODO:  If we have something in inventory, find some place to deposit it.
        // return new MessageStorageRequest()
        return null;
    }

    @Override
    public int getValue()
    {
        // SWAG
        return _priority -10;
    }

    @Override
    public BlockPos selectWorkArea(List<BlockPos> others)
    {
        // TODO:  Use better searcher
        RingedSearcher searcher = new RingedSearcher(getRequester().getPos(), getRequester().getRadius(), 10);
        for (BlockPos pos : searcher)
        {
            if (DrudgeUtils.willDrop(getWorld(), pos, _sample))
            {
                if (!DrudgeUtils.pointInAreas(pos, others, 1))
                    return pos;
            }
        }

        return null;
    }

    //=============

    private boolean findNextBlock()
    {
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
            _harvestList = RingedSearcher.findTree(getEntity().getEntityWorld(), getRequester().getPos(), _radius, _height, _sample);
            if (_harvestList == null)
            {
                // Didn't find any blocks anywhere
                LOGGER.debug("Didn't find any blocks to harvest.  Done.");
                return false;
            }

            // Now that we have a new list, get the next block
            _harvestBlock = _harvestList.poll();
        }

        return true;
    }

    private void startBreaking()
    {
        IBlockState state = getEntity().getEntityWorld().getBlockState(_harvestBlock);
        _breakTotalNeeded = (int)(BASE_BREAK_TIME *
                            state.getBlock().getBlockHardness(getEntity().getEntityWorld(), _harvestBlock) *
                            getEntity().getWorkSpeedFactor());
        _isBreaking = true;
        _breakingProgress = 0;
        _previousBreakProgress = 0;
        LOGGER.debug("Start breaking. Need: " + _breakTotalNeeded);
    }

    /** @return True to continue breaking */
    private boolean handleBreaking()
    {
        _isBreaking = updateBreak();
        if (!_isBreaking)
        {
            LOGGER.debug("Finished breaking, harvesting.");
            harvestBlock();

            if (getEntity().getHeldItem().stackSize >= getEntity().getCarryCapacity() ||
                    getEntity().getHeldItem().stackSize >= _quantity)
            {
                LOGGER.debug("Reached capacity.  Done");
                return false;
            }
            _targetBlock = null;
        }
        return true;
    }


    /** @return True to keep processing. */
    private boolean updateBreak()
    {
        // we have 10 stages
        ++this._breakingProgress;
        int i = (int)((float)this._breakingProgress / _breakTotalNeeded * 10.0F);

        if (i != this._previousBreakProgress)
        {
            getEntity().worldObj.sendBlockBreakProgress(getEntity().getEntityId(), _harvestBlock, i);
            this._previousBreakProgress = i;
        }

        return (this._breakingProgress != _breakTotalNeeded);
    }

    private void harvestBlock()
    {
        ItemStack targetStack = getEntity().getHeldItem();
        if (targetStack == null)
        {
            targetStack = _sample.copy();
            targetStack.stackSize = 0;

            IBlockState blockState = getEntity().getEntityWorld().getBlockState(_harvestBlock);
            //Block blockType = blockState.getBlock();

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

    private boolean startNavigation()
    {
        // At this point we have a harvest block, set a block to walk to
        _targetBlock = new BlockPos(_harvestBlock.getX(), getRequester().getPos().getY(), _harvestBlock.getZ());
        LOGGER.debug("Made new target block.  Moving to: " + _targetBlock);

        return tryMoveTo(_targetBlock);
    }

    public void debugInfo(StringBuilder builder)
    {
        super.debugInfo(builder);
        builder.append(", radius=").append(_radius);
        builder.append(", height=").append(_height);
        builder.append(", quantity=").append(_quantity);
        builder.append(", sample=").append(_sample);
    }

    private final int _radius;
    private final int _height = 10;
    private final int _quantity;
    private final ItemStack _sample;

    private Queue<BlockPos> _harvestList;
    private BlockPos _harvestBlock;
    private BlockPos _targetBlock;

    private int _breakTotalNeeded;
    private int _breakingProgress;
    private int _previousBreakProgress;
    private boolean _isBreaking;

    // How long to break a 1.0 hardness thing in ticks
//    private final int BASE_BREAK_TIME = 40;
    private final int BASE_BREAK_TIME = 20;

    private static final Logger LOGGER = LogManager.getLogger();

}
