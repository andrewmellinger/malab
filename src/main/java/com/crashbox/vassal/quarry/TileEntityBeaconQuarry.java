package com.crashbox.vassal.quarry;

import com.crashbox.vassal.ai.AIUtils;
import com.crashbox.vassal.beacon.BeaconBase;
import com.crashbox.vassal.common.ItemStackMatcher;
import com.crashbox.vassal.messaging.*;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class TileEntityBeaconQuarry extends TileEntity implements IUpdatePlayerListBox, IMessager
{
    public static final String NAME = "tileEntityBeaconQuarry";

    @Override
    public void setWorldObj(World worldIn)
    {
        LOGGER.debug("setWorldObj: " + worldIn);
        super.setWorldObj(worldIn);
        if (worldIn != null && !worldIn.isRemote)
        {
            _quarry = new Quarry(worldIn);
        }
        else
        {
            if (_quarry != null)
                _quarry.terminate();
            _quarry = null;
        }

        // Find Y.
        _currentY = findStartingY();
    }

    @Override
    public void update()
    {
        if (_quarry != null)
            _quarry.update();
    }

    // We need this to know to tear down the forester
    public void blockBroken()
    {
        _quarry.terminate();
    }

    @Override
    public int getRadius()
    {
        return _radius;
    }

    public int getMaxVassalCount()
    {
        // TODO:  Scale based on area.
        return 2;
    }

    private class Quarry extends BeaconBase
    {
        private Quarry(World world)
        {
            super(world);
            LOGGER.debug("Constructing: " + this);
        }

        @Override
        protected IMessager getSender()
        {
            return TileEntityBeaconQuarry.this;
        }

        @Override
        protected void handleMessage(Message msg)
        {
            if (msg instanceof MessageItemRequest)
            {
                handleItemRequest((MessageItemRequest) msg);
            }
//            else if (msg instanceof MessageWorkerAvailability && timeForAvailabilityResponse())
//            {
//                handleWorkerAvailability((MessageWorkerAvailability)msg);
//            }
        }
    }

    private void handleItemRequest(MessageItemRequest msg)
    {
        debugLog("Asked for items." + msg);
    }

    private BlockPos findQuarryCandidate(ItemStackMatcher matcher, List<BlockPos> exclusions)
    {
        // Look around in in our current slab
        BlockPos us = getPos();
        BlockPos start = new BlockPos(us.getX() - _radius, _currentY, us.getZ() - _radius);
        BlockPos end = new BlockPos(us.getX() + _radius, _currentY - 1, us.getZ() + _radius);

        return AIUtils.firstDropOccurrence(getWorld(), start, end, getQuarryMatcher());
    }

    private int findStartingY()
    {
        BlockPos us = getPos();
        BlockPos start = new BlockPos(us.getX() - _radius, us.getY() -1, us.getZ() - _radius);
        BlockPos end = new BlockPos(us.getX() + _radius, 11, us.getZ() + _radius);

        BlockPos first = AIUtils.firstDropOccurrence(getWorld(), start, end, getQuarryMatcher());
        if (first != null)
            return first.getY();

        return 10;
    }


    private void debugLog(String msg)
    {
        LOGGER.debug("Quarry: " + msg);
    }


    @Override
    public String toString()
    {
        return "Quarry{" +
                "_quarry=" + _quarry +
                '}';
    }



    private Quarry _quarry;

    // QUARRIES ARE 7x7
    private int _radius = 3;

    public static ItemStackMatcher getQuarryMatcher()
    {
        if (QUARRY_MATCHER != null)
            return QUARRY_MATCHER;

        ItemStackMatcher matcher = new ItemStackMatcher();
        matcher.add(new ItemStack(Item.getItemFromBlock(Blocks.cobblestone)));
        matcher.add(new ItemStack(Item.getItemFromBlock(Blocks.gravel)));
        matcher.add(new ItemStack(Item.getItemFromBlock(Blocks.coal_ore)));

        QUARRY_MATCHER = matcher;

        return QUARRY_MATCHER;
    }

    private static ItemStackMatcher QUARRY_MATCHER;

    private int _currentY = Integer.MAX_VALUE;

    private static final Logger LOGGER = LogManager.getLogger();

}
