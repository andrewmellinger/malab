package com.crashbox.mal.quarry;

import com.crashbox.mal.MALMain;
import com.crashbox.mal.task.TaskQuarryTop;
import com.crashbox.mal.util.MALUtils;
import com.crashbox.mal.ai.EntityAIWorkDroid;
import com.crashbox.mal.ai.Priority;
import com.crashbox.mal.autoblock.AutoBlockBase;
import com.crashbox.mal.common.AnyItemMatcher;
import com.crashbox.mal.common.ItemStackMatcher;
import com.crashbox.mal.workdroid.EntityWorkDroid;
import com.crashbox.mal.messaging.*;
import com.crashbox.mal.task.TaskQuarry;
import com.crashbox.mal.util.StairBuilder;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class TileEntityAutoQuarry extends TileEntity implements IUpdatePlayerListBox, IMessager
{
    public static final String NAME = "tileEntityAutoQuarry";

    @Override
    public void setWorldObj(World worldIn)
    {
        LOGGER.debug("setWorldObj: " + worldIn);
        super.setWorldObj(worldIn);
        if (worldIn != null && !worldIn.isRemote)
        {
            _quarry = new Quarry(worldIn);
            _broadcastHelper = new Broadcaster.BroadcastHelper(worldIn.provider.getDimensionId());
        } else
        {
            if (_quarry != null)
                _quarry.terminate();
            _quarry = null;
            _broadcastHelper = null;
        }
    }

    @Override
    public void update()
    {
        if (worldObj.isRemote)
            return;

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

    @Override
    public BlockPos getBlockPos()
    {
        return getPos();
    }

    private class Quarry extends AutoBlockBase
    {
        private Quarry(World world)
        {
            super(world);
            LOGGER.debug("Constructing: " + this);
        }

        @Override
        protected IMessager getSender()
        {
            return TileEntityAutoQuarry.this;
        }

        @Override
        protected int concurrentWorkerCount()
        {
            // We can't handle that many
            return 2;
        }

        @Override
        protected void handleMessage(Message msg)
        {
            if (msg instanceof MessageItemRequest)
            {
                if (haveFreeWorkerSlots(msg))
                    handleItemRequest((MessageItemRequest) msg);
            }
            else if (msg instanceof MessageWorkerAvailability)
            {
                if (haveFreeWorkerSlots(msg) && readyForNextAvailabilityResponseMS())
                    handleWorkerAvailability((MessageWorkerAvailability) msg);
            }
        }
    }

    private void handleItemRequest(MessageItemRequest msg)
    {
        // First, if we need stairs, send a stairs event
        StairBuilder builder = new StairBuilder(getWorld(), getPos(), _radius);

        LOGGER.debug("handleItemRequest: " + msg.getMatcher() + ", from=" + msg.getSender());
        if (builder.findFirstQuarryable(msg.getMatcher(), getEntityFromMessage(msg), null) != null)
        {
            LOGGER.debug("--found item.");
            TRHarvest quarry = new TRHarvest(this, msg.getSender(), msg.getTransactionID(),
                    Priority.getQuarryItemHarvestValue(getWorld()),
                    TaskQuarry.class, msg.getMatcher(), 1);
            _broadcastHelper.postMessage(quarry);
        }
    }

    private void handleWorkerAvailability(MessageWorkerAvailability msg)
    {
        debugLog("handleWorkerAvailability=" + msg);

        if (getPos().getY() <= 11)
            return;

            // first clean up any messes
        if (MALUtils.generateCleanupTask(this, getWorld(), getPos(), _radius, msg))
        {
            _quarry.setNextAvailabilityResponseMS();
            return;
        }

        // If we need stairs, send a stairs event
        StairBuilder builder = new StairBuilder(getWorld(), getPos(), _radius);

        // First, clear out area around quarry except for cobblestone stairs.
        List<BlockPos> exclusions = new ArrayList<BlockPos>();
        exclusions.add(getBlockPos());

        BlockPos pos = builder.findTopQuarryable(StairBuilder.getNotStairMatcher(), getEntityFromMessage(msg),
                exclusions);
        if (pos != null)
        {
            TRHarvest quarry = new TRHarvest(this, msg.getSender(), msg.getTransactionID(),
                    Priority.getQuarryCleanTopValue(), TaskQuarryTop.class, StairBuilder.getNotStairMatcher(), -1);

            _quarry.setNextAvailabilityResponseMS();
            _broadcastHelper.postMessage(quarry);
            //LOGGER.debug("Posted: " + quarry);
            return;
        }

        // Set it up.
        builder.findNextStair();

        int stairsNeeded = builder.getNeededStairCount();
        if (stairsNeeded > 0)
        {
            //LOGGER.debug("Found  first stair.");
            TRMakeBigStair makeStair = new TRMakeBigStair(this, msg.getSender(), msg.getTransactionID(),
                    Priority.getStairBuilderValue(), stairsNeeded);

            _quarry.setNextAvailabilityResponseMS();
            _broadcastHelper.postMessage(makeStair);

            // If we can do stairs, then we don't want to ask for anything else.
            return;
        }

        // If we have something to harvest, call him over
        if (builder.findFirstQuarryable(new AnyItemMatcher(), getEntityFromMessage(msg), null) != null)
        {
            int value = Priority.getQuarryIdleHarvestValue(getWorld());

            // Add some value the closer we get to the bottom.
            value += Priority.quarryDepthValue(getWorld(), msg.getSender().getBlockPos().getY());

            TRHarvest quarry = new TRHarvest(this, msg.getSender(), msg.getTransactionID(),
                    value, TaskQuarry.class, new AnyItemMatcher(), -1);

            _quarry.setNextAvailabilityResponseMS();
            _broadcastHelper.postMessage(quarry);
            //LOGGER.debug("Posted: " + quarry);
            return;
        }

        // If we are here we need a worker to move us down one.
        if (getPos().getY() > 11)
        {
            _quarry.setNextAvailabilityResponseMS();
            MALUtils.postHarvestPlacePair(getWorld(), this, msg,
                    Priority.getQuarryMoveQuarryBlockValue(), Priority.getQuarryMoveQuarryBlockValue(),
                    new ItemStackMatcher(MALMain.BLOCK_AUTO_QUARRY), getPos(), getPos().down(),
                    false);
        }
    }


    private EntityWorkDroid getEntityFromMessage(Message msg)
    {
        if (msg.getSender() instanceof EntityAIWorkDroid)
        {
            EntityAIWorkDroid entityAI = (EntityAIWorkDroid) msg.getSender();
            return entityAI.getEntity();
        }
        return null;
    }


    private void debugLog(String msg)
    {
        LOGGER.debug(msg);
    }

    @Override
    public String toString()
    {
        return "TEBQuarry@" + Integer.toHexString(this.hashCode()) + "{}";
    }

    private Quarry _quarry;
    private Broadcaster.BroadcastHelper _broadcastHelper;

    private int _radius = 3;

    private static final Logger LOGGER = LogManager.getLogger();
}
