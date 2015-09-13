package com.crashbox.drudgemod.task;

import com.crashbox.drudgemod.DrudgeUtils;
import com.crashbox.drudgemod.ai.AIUtils;
import com.crashbox.drudgemod.ai.EntityAIDrudge;
import com.crashbox.drudgemod.ai.RingedSearcher;
import com.crashbox.drudgemod.common.ItemStackMatcher;
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
public class TaskHarvest extends TaskAcquireBase
{
    public TaskHarvest(EntityAIDrudge performer, MessageHarvestRequest message)
    {
        super(performer, message.getSender(), message.getValue());
        _radius = message.getSender().getRadius();
        _quantity = message.getQuantity();
        _matcher = message.getMatcher();
    }

    @Override
    public boolean executeAndIsDone()
    {
        // REMEMBER: Return true to STOP
        // If we are in the process of breaking, do that.
        if (_isBreaking)
        {
            if (continueBreaking())
            {
                return false;
            }

            debugLog(LOGGER, "HarvestList: " + _harvestList);
            // Have we done enough.
            if (getEntity().isHeldInventoryFull() || getEntity().getHeldSize() >= _quantity)
                return true;

            if (_harvestList == null || _harvestList.peek() == null)
            {
                return true;
            }

            // Keep going on this tree
            _harvestBlock = _harvestList.poll();
            if (_harvestBlock == null)
                return true;

            startBreaking();
            return false;
        }


        if (_harvestBlock == null)
        {
            debugLog(LOGGER, "Got to end of harvest executeAndIsDone with null harvestBlock...");
            return true;
        }

        // Otherwise start breaking
        startBreaking();
        return false;
    }

    @Override
    public int getValue()
    {
        // SWAG
        return _priority - 10;
    }

    @Override
    public BlockPos chooseWorkArea(List<BlockPos> others)
    {
        _harvestBlock = null;
        if (_harvestList != null)
            _harvestBlock = _harvestList.poll();

        if (_harvestBlock == null)
            _harvestList = RingedSearcher.findTree(getEntity().getEntityWorld(), getRequester().getPos(), _radius,
                    _height, _matcher, others);

        if (_harvestList == null || _harvestList.isEmpty())
        {
            _harvestBlock = null;
            _harvestList = null;
            return null;
        }

        _harvestBlock = _harvestList.poll();

        // We want them to move to a place which is correct X,Z but with ground Y.

        return new BlockPos(_harvestBlock.getX(), getRequester().getPos().getY(), _harvestBlock.getZ());
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
        //debugLog(LOGGER, "Start breaking. Need: " + _breakTotalNeeded);
    }

    /** @return True to continue harvesting */
    private boolean continueBreaking()
    {
        _isBreaking = updateBreak();
        if (!_isBreaking)
        {
            //debugLog(LOGGER, "Finished breaking, harvesting.");
            if (harvestBlock())
            {
                // We need to find another harvest block
                _harvestBlock = null;
                return false;
            }
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

        return (this._breakingProgress < _breakTotalNeeded);
    }

    // Return true if harvested
    private boolean harvestBlock()
    {
        ItemStack targetStack = getEntity().getHeldItem();
        if (targetStack == null)
        {
            targetStack = DrudgeUtils.identifyWillDrop(getWorld(), _harvestBlock, _matcher);
        }
        else
        {
            // It changed or won't drop the right thing, bail.
            if (!DrudgeUtils.willDrop(getEntity().getEntityWorld(), _harvestBlock, new ItemStackMatcher(targetStack)))
                return false;
        }

        LOGGER.debug("Quantity BEFORE harvest " + targetStack.stackSize);

        ///// BREAK
        getEntity().getEntityWorld().destroyBlock(_harvestBlock, true);

        ///// PICKUP
        AIUtils.collectEntityIntoStack(getEntity().getEntityWorld(), _harvestBlock, 3, targetStack);

        LOGGER.debug("Quantity AFTER harvest " + targetStack.stackSize);

        if (getEntity().getHeldItem() == null && targetStack.stackSize > 0)
            getEntity().setCurrentItemOrArmor(0, targetStack);

        return true;
    }

    public void debugInfo(StringBuilder builder)
    {
        super.debugInfo(builder);
        builder.append(", harvestBlock=").append(_harvestBlock);
        builder.append(", radius=").append(_radius);
        builder.append(", height=").append(_height);
        builder.append(", quantity=").append(_quantity);
        builder.append(", matcher=").append(_matcher);
    }

    // Describes the search area
    private final int _radius;
    private final int _height = 10;
    private final int _quantity;
    private final ItemStackMatcher _matcher;

    // Blocks we are breaking
    private Queue<BlockPos> _harvestList;
    private BlockPos _harvestBlock;

    // Spot on ground we move to.
//    private BlockPos _targetBlock;

    private int _breakTotalNeeded;
    private int _breakingProgress;
    private int _previousBreakProgress;
    private boolean _isBreaking;

    // How long to break a 1.0 hardness thing in ticks
//    private final int BASE_BREAK_TIME = 40;
    private final int BASE_BREAK_TIME = 20;

    private static final Logger LOGGER = LogManager.getLogger();

}
