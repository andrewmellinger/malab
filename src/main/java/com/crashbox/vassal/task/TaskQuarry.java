package com.crashbox.vassal.task;

import com.crashbox.vassal.VassalUtils;
import com.crashbox.vassal.ai.EntityAIVassal;
import com.crashbox.vassal.common.ItemStackMatcher;
import com.crashbox.vassal.messaging.IMessager;
import com.crashbox.vassal.messaging.TRQuarry;
import com.crashbox.vassal.quarry.TileEntityBeaconQuarry;
import com.crashbox.vassal.util.StairBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import java.util.List;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class TaskQuarry extends TaskAcquireBase
{
    public TaskQuarry(EntityAIVassal performer, IMessager requester, TRQuarry taskRequest)
    {
        super(performer, requester, taskRequest.getValue(), taskRequest.getMatcher());
        _cobblestoneMatcher = new ItemStackMatcher(new ItemStack(Blocks.cobblestone));
    }

    @Override
    public BlockPos chooseWorkArea(List<BlockPos> others)
    {

        if (_builder == null)
            _builder = new StairBuilder(getWorld(), getRequester().getPos(), getRequester().getRadius());

        ItemStack held = getPerformer().getEntity().getHeldItem();
        if (_makingStairs && held != null && _stairBlock != null)
        {
            // We have something in hand, so go to the stairs.  We came here from _harvest
            return _stairBlock;
        }

        if (_makingStairs && _builder.findNextStair())
        {
            // We need to build a stair.
            _stairBlock = _builder.getStair();

            // If we can't harvest it, harvest another
            if (getWorld().isAirBlock(_stairBlock))
            {
                _harvestBlock = _builder.findFirstQuarryable(_cobblestoneMatcher);
                return _harvestBlock;
            }

            _harvestBlock = null;
            return _stairBlock;
        }
        else
        {
            _makingStairs = false;
            // Just harvest a block

            _harvestBlock = _builder.findFirstQuarryable(TileEntityBeaconQuarry.getQuarryMatcher());
            return _harvestBlock;
        }
    }

    @Override
    public UpdateResult executeAndIsDone()
    {
        if (_makingStairs)
        {
            if (_harvestBlock != null && _stairBlock != null && _stairBlock.equals(_harvestBlock))
            {
                // Harvest the block and retarget
                VassalUtils.harvestBlock(getWorld(), getEntity(), _harvestBlock, _cobblestoneMatcher);
                return UpdateResult.RETARGET;
            }
            else if (_stairBlock != null)
            {
                // Place the block, and retarget
                placeStairBlock();
                return UpdateResult.RETARGET;
            }
        }

        // We are at the block.  Break it, see if we are done
        VassalUtils.harvestBlock(getWorld(), getEntity(), _harvestBlock, TileEntityBeaconQuarry.getQuarryMatcher());

        if (getEntity().isHeldInventoryFull())
            return UpdateResult.DONE;

        return UpdateResult.RETARGET;
    }

    public void placeStairBlock()
    {
        // Make sure the block is air
        if (!getWorld().isAirBlock(_stairBlock))
            return;

        ItemStack held = getEntity().getHeldItem();
        if (held == null || held.stackSize == 0)
            return;

        held.stackSize -= 1;
        if (held.stackSize == 0)
            getEntity().setCurrentItemOrArmor(0, null);

        getWorld().setBlockState(_stairBlock, _builder.getStairState());
    }


    // If these are different we needed to harvest something else
    private BlockPos _harvestBlock;
    private BlockPos _stairBlock;


    private ItemStackMatcher _cobblestoneMatcher;
    private StairBuilder _builder;
    private boolean _makingStairs = true;
}
