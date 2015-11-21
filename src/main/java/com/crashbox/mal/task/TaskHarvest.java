package com.crashbox.mal.task;

import com.crashbox.mal.util.VassalUtils;
import com.crashbox.mal.ai.EntityAIVassal;
import com.crashbox.mal.messaging.TRHarvest;
import com.crashbox.mal.task.ITask.UpdateResult;
import com.crashbox.mal.util.BlockBreaker;
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
    public BlockPos getWorkTarget(List<BlockPos> others)
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
        return new BlockPos(_harvestBlock.getX(), getRequester().getBlockPos().getY(), _harvestBlock.getZ());
    }

    @Override
    public UpdateResult executeAndIsDone()
    {
        //debugLog(LOGGER, "executeAndDone");
        if (_breaker == null)
        {
            _breaker = new BlockBreaker(getWorld(), getEntity(), _harvestBlock);
            return UpdateResult.CONTINUE;
        }

        if (_breaker.isStillBreaking())
            return UpdateResult.CONTINUE;

        // We are done breaking.
        _breaker = null;

        //debugLog(LOGGER, "Finished breaking, harvesting.");
        VassalUtils.harvestBlockIntoHeld(getWorld(), getEntity(), _harvestBlock, getMatcher());
        // This is kinda a hack...
        onBlockBroken(_harvestBlock);
        _harvestBlock = null;

        // Have we done enough.
        if (getEntity().isHeldInventoryFull() || getEntity().getHeldSize() >= _quantity)
            return UpdateResult.DONE;

        // If we have another block, let's keep going
        _harvestBlock = _harvestList.poll();
        if (_harvestBlock == null)
            return UpdateResult.DONE;

        // If we got here, let's start breaking again
        _breaker = new BlockBreaker(getWorld(), getEntity(), _harvestBlock);
        return UpdateResult.CONTINUE;
    }

    // This adds the specific algorithm that find trees, or blocks of stone,or whatever
    protected abstract Queue<BlockPos> findHarvestList(List<BlockPos> exclusions);

    private void startBreaking()
    {
        _breaker = new BlockBreaker(getWorld(), getEntity(), _harvestBlock);
    }

    // Place for subclass to inject logic
    protected void onBlockBroken(BlockPos pos)
    {

    }

    //====

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
    protected final int _height = 20;
    protected final int _quantity;

    // Blocks we are breaking
    private Queue<BlockPos> _harvestList;
    private BlockPos _harvestBlock;

    // Animating breaking
    private BlockBreaker _breaker;

    private static final Logger LOGGER = LogManager.getLogger();

}
