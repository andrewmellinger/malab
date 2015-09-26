package com.crashbox.vassal.quarry;

import com.crashbox.vassal.VassalMain;
import com.crashbox.vassal.ai.EntityAIVassal;
import com.crashbox.vassal.beacon.BeaconBase;
import com.crashbox.vassal.common.AnyItemMatcher;
import com.crashbox.vassal.common.ItemStackMatcher;
import com.crashbox.vassal.messaging.*;
import com.crashbox.vassal.task.TaskQuarry;
import com.crashbox.vassal.util.StairBuilder;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemTool;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
        } else
        {
            if (_quarry != null)
                _quarry.terminate();
            _quarry = null;
        }
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
        if (_quarry != null)
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
            if (msg instanceof MessageWorkerAvailability && timeForAvailabilityResponse())
            {
                debugLog("Handling worker availability.");
                handleWorkerAvailability((MessageWorkerAvailability) msg);
            }
        }
    }

    private void handleItemRequest(MessageItemRequest msg)
    {
        // First, if we need stairs, send a stairs event
        StairBuilder builder = new StairBuilder(getWorld(), getPos(), _radius);

        LOGGER.debug("Quarry: Got item request: " + msg.getMatcher());
//        ItemTool tool = getEntityPickaxe(msg);
        ItemTool tool = null;
        if (builder.findFirstQuarryable(msg.getMatcher(), tool) != null)
        {
            LOGGER.debug("Quarry: Found item.");
            TRHarvest quarry = new TRHarvest(this, msg.getSender(), msg.getTransactionID(), 0,
                    TaskQuarry.class, msg.getMatcher(), 1);
            Broadcaster.postMessage(quarry);
        }
    }

    private void handleWorkerAvailability(MessageWorkerAvailability msg)
    {
        // First, if we need stairs, send a stairs event
        StairBuilder builder = new StairBuilder(getWorld(), getPos(), _radius);

        // Set it up.
        builder.findNextStair();

        int stairsNeeded = builder.getNeededStairCount();
        if (stairsNeeded > 0)
        {
            LOGGER.debug("Found  first stair.");
            TRMakeBigStair makeStair = new TRMakeBigStair(this, msg.getSender(), msg.getTransactionID(), 10, stairsNeeded);
            Broadcaster.postMessage(makeStair);
            return;
        }

        // If we have something that will drop, call him over
//        ItemTool tool = getEntityPickaxe(msg);
        ItemTool tool = null;
        if (builder.findFirstQuarryable(new AnyItemMatcher(), tool) != null)
        {
            TRHarvest quarry = new TRHarvest(this, msg.getSender(), msg.getTransactionID(), 0,
                    TaskQuarry.class, new AnyItemMatcher(), 1);
            Broadcaster.postMessage(quarry);
            return;
        }

        // If we are here we need a worker to move us down one.
        TRHarvestBlock harvest = new TRHarvestBlock(this, msg.getSender(), msg.getTransactionID(),
                20, new ItemStackMatcher(VassalMain.BLOCK_BEACON_QUARRY), getPos());

        TRPlaceBlock place = new TRPlaceBlock(this, msg.getSender(), msg.getTransactionID(),
                20, getPos().down());

        MessageTaskPairRequest pair = new MessageTaskPairRequest(this, msg.getSender(), msg.getTransactionID(),
                false, harvest, place);
        Broadcaster.postMessage(pair);
    }

//    private BlockPos findQuarryCandidate()
//    {
//        // Look around in in our current slab
//        BlockPos us = getPos();
//        BlockPos start = new BlockPos(us.getX() - _radius, us.getY(), us.getZ() - _radius);
//        BlockPos end = new BlockPos(us.getX() + _radius, us.getY() - 1, us.getZ() + _radius);
//
//        return VassalUtils.firstDropOccurrence(getWorld(), start, end, ItemStackMatcher.getQuarryMatcher());
//    }

//    private int findStartingY()
//    {
//        BlockPos us = getPos();
//        BlockPos start = new BlockPos(us.getX() - _radius, us.getY() -1, us.getZ() - _radius);
//        BlockPos end = new BlockPos(us.getX() + _radius, 11, us.getZ() + _radius);
//
//        BlockPos first = VassalUtils.firstDropOccurrence(getWorld(), start, end,
//                ItemStackMatcher.getQuarryMatcher());
//        if (first != null)
//            return first.getY();
//
//        return 10;
//    }

    private ItemTool getEntityPickaxe(Message msg)
    {
        ItemTool tool = (ItemTool) Items.stone_pickaxe;
        if(msg.getSender()instanceof EntityAIVassal)

        {
            Item item = ((EntityAIVassal) msg.getSender()).getEntity().getCurrentPickAxe().getItem();
            if (item instanceof ItemTool)
                tool = (ItemTool) item;
        }
        return tool;
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

    private int _radius = 3;

    private static final Logger LOGGER = LogManager.getLogger();
}
