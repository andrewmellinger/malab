package com.crashbox.mal.task;

import com.crashbox.mal.util.MALUtils;
import com.crashbox.mal.ai.EntityAIWorkDroid;
import com.crashbox.mal.common.ItemStackMatcher;
import com.crashbox.mal.messaging.TRMakeBigStair;
import com.crashbox.mal.task.ITask.UpdateResult;
import com.crashbox.mal.util.BlockBreaker;
import com.crashbox.mal.util.StairBuilder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Copyright CMU 2015.
 */
public class TaskMakeBigStair extends TaskDeliverBase
{
    public TaskMakeBigStair(EntityAIWorkDroid performer, TRMakeBigStair message)
    {
        super(performer, message.getSender(), message.getValue());
        _matcher = message.getMatcher();

        // TODO: Make sure we have slab or cobblestone
        _quantity = message.getQuantity();
        _builder = new StairBuilder(getWorld(), getRequester().getBlockPos(), getRequester().getRadius());
    }

    @Override
    public BlockPos getWorkTarget(List<BlockPos> others)
    {
        // If we can't find any needed stair block  then we are done.
        if (!_builder.findNextStair())
            return null;

        debugLog(LOGGER, "Found stair block candidate.");

        // We need to build a stair.
        return _builder.getStairPos();
    }

    @Override
    public UpdateResult executeAndIsDone()
    {
        // TODO:  Add break animation
        if (_breaker == null)
        {
            // Check to make sure it isn't stair.  Someone else may have already done this...
            IBlockState state = getWorld().getBlockState(_builder.getStairPos());
            if (state == _builder.getStairState())
                return UpdateResult.RETARGET;

            _breaker = new BlockBreaker(getWorld(), getEntity(), _builder.getStairPos());
            return UpdateResult.CONTINUE;
        }

        if (_breaker.isStillBreaking())
            return UpdateResult.CONTINUE;

        // We are done breaking.
        _breaker = null;

        // If something is there, then break it.
        MALUtils.harvestBlockIntoHeld(getWorld(), getPerformer().getEntity(), _builder.getStairPos(),
                ItemStackMatcher.getQuarryMatcher());

        if (getWorld().isAirBlock(_builder.getStairPos()))
        {
            // Place the block, and retarget
            debugLog(LOGGER, "place and retarget");
            placeStairBlock();
        }

        BlockPos downOne = _builder.getStairPos().down();
        if (getWorld().isAirBlock(downOne))
            getEntity().placeHeldBlock(getWorld(), downOne);

        // If we have more inventory, let's try to do this again.  GetWorkTarget will return null if no more spaces.
        if (getEntity().getHeldSize() == 0)
            return UpdateResult.DONE;

        return UpdateResult.RETARGET;
    }

    public void placeStairBlock()
    {
        // Make sure the block is air
        if (!getWorld().isAirBlock(_builder.getStairPos()))
            return;

        ItemStack held = getEntity().getHeldItem();
        if (held == null || held.stackSize == 0)
            return;

        // TODO:  Make sure we have cobblestone or cobblestone stair

        held.stackSize -= 1;
        if (held.stackSize == 0)
            getEntity().setCurrentItemOrArmor(0, null);

        getWorld().setBlockState(_builder.getStairPos(), _builder.getStairState());
    }


    // If these are different we needed to harvest something else
    private final StairBuilder _builder;
    private BlockBreaker _breaker;

    private static final Logger LOGGER = LogManager.getLogger();}
