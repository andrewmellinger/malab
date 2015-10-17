package com.crashbox.vassal.quarry;

import com.crashbox.vassal.VassalMain;
import com.crashbox.vassal.util.VassalUtils;
import com.crashbox.vassal.ai.EntityAIVassal;
import com.crashbox.vassal.ai.Priority;
import com.crashbox.vassal.beacon.BeaconBase;
import com.crashbox.vassal.common.AnyItemMatcher;
import com.crashbox.vassal.common.ItemStackMatcher;
import com.crashbox.vassal.entity.EntityVassal;
import com.crashbox.vassal.messaging.*;
import com.crashbox.vassal.task.TaskQuarry;
import com.crashbox.vassal.util.StairBuilder;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
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

    @Override
    public BlockPos getBlockPos()
    {
        return getPos();
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
        protected int concurrentWorkerCount()
        {
            // We can't handle that many
            return 1;
        }

        @Override
        protected void handleMessage(Message msg)
        {
            if (msg instanceof MessageItemRequest)
            {
                if (haveFreeWorkerSlots())
                    handleItemRequest((MessageItemRequest) msg);
            }
            else if (msg instanceof MessageWorkerAvailability)
            {
                if (haveFreeWorkerSlots())
                {
                    debugLog("Handling worker availability.");
                    handleWorkerAvailability((MessageWorkerAvailability) msg);
                }
            }
        }
    }

    private void handleItemRequest(MessageItemRequest msg)
    {
        // First, if we need stairs, send a stairs event
        StairBuilder builder = new StairBuilder(getWorld(), getPos(), _radius);

        LOGGER.debug("Quarry: Got item request: " + msg.getMatcher() + ", from=" + msg.getSender());
        if (builder.findFirstQuarryable(msg.getMatcher(), getEntityFromMessage(msg)) != null)
        {
            LOGGER.debug("Quarry: Found item.");
            TRHarvest quarry = new TRHarvest(this, msg.getSender(), msg.getTransactionID(),
                    Priority.getQuarryItemHarvestValue(),
                    TaskQuarry.class, msg.getMatcher(), 1);
            Broadcaster.postMessage(quarry);
        }
    }

    private void handleWorkerAvailability(MessageWorkerAvailability msg)
    {
        // first clean up any messes
        if (VassalUtils.generateCleanupTask(this, getWorld(), getPos(), _radius, msg))
            return;

        // If we need stairs, send a stairs event
        StairBuilder builder = new StairBuilder(getWorld(), getPos(), _radius);

        // Set it up.
        builder.findNextStair();

        int stairsNeeded = builder.getNeededStairCount();
        if (stairsNeeded > 0)
        {
            //LOGGER.debug("Found  first stair.");
            TRMakeBigStair makeStair = new TRMakeBigStair(this, msg.getSender(), msg.getTransactionID(),
                    Priority.getStairBuilderValue(), stairsNeeded);
            Broadcaster.postMessage(makeStair);

            // If we can do stairs, then we don't want to ask for anything else.
            return;
        }

        // If we have something that will drop, call him over
        if (builder.findFirstQuarryable(new AnyItemMatcher(), getEntityFromMessage(msg)) != null)
        {
            int value = Priority.getQuarryIdleHarvestingValue();

            // Add some value the closer we get to the bottom.
            value += Priority.quarryDepthValue(msg.getSender().getBlockPos().getY());

            TRHarvest quarry = new TRHarvest(this, msg.getSender(), msg.getTransactionID(),
                    value, TaskQuarry.class, new AnyItemMatcher(), 1);
            Broadcaster.postMessage(quarry);
            return;
        }

        // If we are here we need a worker to move us down one.
        VassalUtils.postHarvestPlacePair(this, msg,
                Priority.getQuarryMoveQuarryBlockValue(), Priority.getQuarryMoveQuarryBlockValue(),
                new ItemStackMatcher(VassalMain.BLOCK_BEACON_QUARRY), getPos(), getPos().down(),
                false);
    }


    private EntityVassal getEntityFromMessage(Message msg)
    {
        if (msg.getSender() instanceof EntityAIVassal)
        {
            EntityAIVassal entityAI = (EntityAIVassal) msg.getSender();
            return entityAI.getEntity();
        }
        return null;
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
