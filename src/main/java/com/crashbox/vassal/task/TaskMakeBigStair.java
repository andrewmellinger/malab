package com.crashbox.vassal.task;

import com.crashbox.vassal.VassalUtils;
import com.crashbox.vassal.ai.EntityAIVassal;
import com.crashbox.vassal.common.ItemStackMatcher;
import com.crashbox.vassal.messaging.TRMakeBigStair;
import com.crashbox.vassal.task.ITask.UpdateResult;
import com.crashbox.vassal.util.StairBuilder;
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
    public TaskMakeBigStair(EntityAIVassal performer, TRMakeBigStair message)
    {
        super(performer, message.getSender(), message.getValue());
        _matcher = message.getMatcher();

        // TODO: Make sure we have slab or cobblestone
        _quantity = message.getQuantity();
        _builder = new StairBuilder(getWorld(), getRequester().getPos(), getRequester().getRadius());
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

        // Check to make sure it isn't stair.  Someone else may have already done this...
        IBlockState state = getWorld().getBlockState(_builder.getStairPos());
        if (state == _builder.getStairState())
            return UpdateResult.RETARGET;

        // If something is there, then break it.
        VassalUtils.harvestBlockIntoHeld(getWorld(), getPerformer().getEntity(), _builder.getStairPos(),
                ItemStackMatcher.getQuarryMatcher());

        if (getWorld().isAirBlock(_builder.getStairPos()))
        {
            // Place the block, and retarget
            debugLog(LOGGER, "place and retarget");
            placeStairBlock();
            return UpdateResult.RETARGET;
        }

        // If we have more inventory, let's try to do this again
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

    private static final Logger LOGGER = LogManager.getLogger();}
