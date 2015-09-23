package com.crashbox.vassal.task;

import com.crashbox.vassal.VassalUtils;
import com.crashbox.vassal.ai.EntityAIVassal;
import com.crashbox.vassal.ai.RingedSearcher;
import com.crashbox.vassal.messaging.TRHarvest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Queue;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public abstract class TaskHarvest extends TaskAcquireBase
{
    public TaskHarvest(EntityAIVassal performer, TRHarvest message)
    {
        super(performer, message.getSender(), message.getValue(), message.getMatcher());
        _radius = message.getSender().getRadius();
        _quantity = message.getQuantity() == -1 ? performer.getEntity().getCarryCapacity() : message.getQuantity();
    }

    @Override
    public BlockPos chooseWorkArea(List<BlockPos> others)
    {
        _harvestBlock = null;
        if (_harvestList != null)
            _harvestBlock = _harvestList.poll();

        if (_harvestBlock == null)
            _harvestList = findHarvestList(others);

        if (_harvestList == null || _harvestList.isEmpty())
        {
            _harvestBlock = null;
            _harvestList = null;
            return null;
        }

        _harvestBlock = _harvestList.poll();

        // By now we should have one because it shouldn't contain null...
        if (_harvestBlock == null)
            return null;

        // We want them to move to a place which is correct X,Z but with ground Y.
        return new BlockPos(_harvestBlock.getX(), getRequester().getPos().getY(), _harvestBlock.getZ());
    }

    @Override
    public UpdateResult executeAndIsDone()
    {
        // If we are in the process of breaking, do that.
        if (_isBreaking)
        {
            if (continueBreaking())
            {
                return UpdateResult.CONTINUE;
            }

            //debugLog(LOGGER, "HarvestList: " + _harvestList);
            // Have we done enough.
            if (getEntity().isHeldInventoryFull() || getEntity().getHeldSize() >= _quantity)
                return UpdateResult.DONE;

            if (_harvestList == null || _harvestList.peek() == null)
            {
                return UpdateResult.DONE;
            }

            // Keep going on this tree
            _harvestBlock = _harvestList.poll();
            if (_harvestBlock == null)
                return UpdateResult.DONE;

            startBreaking();
            return UpdateResult.CONTINUE;
        }


        if (_harvestBlock == null)
        {
            debugLog(LOGGER, "Got to end of harvest executeAndIsDone with null harvestBlock...");
            return UpdateResult.DONE;
        }

        // Otherwise start breaking
        startBreaking();
        return UpdateResult.CONTINUE;
    }

    @Override
    public int getValue()
    {
        // SWAG
        return _value - 10;
    }


    // This adds the specific algorithm that find trees, or blocks of stone,or whatever
    protected abstract Queue<BlockPos> findHarvestList(List<BlockPos> others);
//    _harvestList = RingedSearcher.findTree(getEntity().getEntityWorld(), getRequester().getPos(), _radius,
//    _height, _matcher, others);


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
            VassalUtils.harvestBlockIntoHeld(getWorld(), getEntity(), _harvestBlock, getMatcher());
            // We need to find another harvest block
            _harvestBlock = null;
            return false;
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

    public void debugInfo(StringBuilder builder)
    {
        super.debugInfo(builder);
        builder.append(", harvestBlock=").append(_harvestBlock);
        builder.append(", radius=").append(_radius);
        builder.append(", height=").append(_height);
        builder.append(", quantity=").append(_quantity);
    }

    // Describes the search area
    protected final int _radius;
    protected final int _height = 10;
    protected final int _quantity;

    // Blocks we are breaking
    private Queue<BlockPos> _harvestList;
    private BlockPos _harvestBlock;

    private int _breakTotalNeeded;
    private int _breakingProgress;
    private int _previousBreakProgress;
    private boolean _isBreaking;

    // How long to break a 1.0 hardness thing in ticks
//    private final int BASE_BREAK_TIME = 40;
    private final int BASE_BREAK_TIME = 20;

    private static final Logger LOGGER = LogManager.getLogger();

}
