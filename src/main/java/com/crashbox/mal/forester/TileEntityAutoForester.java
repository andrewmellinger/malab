package com.crashbox.mal.forester;

import com.crashbox.mal.MALMain;
import com.crashbox.mal.util.MALUtils;
import com.crashbox.mal.ai.*;
import com.crashbox.mal.autoblock.AutoBlockBase;
import com.crashbox.mal.common.ItemStackMatcher;
import com.crashbox.mal.common.ItemTypeMatcher;
import com.crashbox.mal.messaging.*;
import com.crashbox.mal.task.TaskHarvestTree;
import com.crashbox.mal.util.RingedSearcher;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class TileEntityAutoForester extends TileEntity implements IUpdatePlayerListBox, IMessager
{
    public static final String NAME = "tileEntityAutoForester";

    //=============================================================================================
    // ##### ##### #     ##### ##### #   # ##### ##### ##### #   #
    //   #     #   #     #     #     ##  #   #     #     #    # #
    //   #     #   #     ####  ####  # # #   #     #     #     #
    //   #     #   #     #     #     #  ##   #     #     #     #
    //   #   ##### ##### ##### ##### #   #   #   #####   #     #

    @Override
    public void setWorldObj(World worldIn)
    {
        LOGGER.debug("setWorldObj: " + worldIn);
        super.setWorldObj(worldIn);
        if (worldIn != null && !worldIn.isRemote)
        {
            _forester = new Forester(worldIn);
            _broadcastHelper = new Broadcaster.BroadcastHelper(worldIn.provider.getDimensionId());
        }
        else
        {
            if (_forester != null)
                _forester.terminate();
            _forester = null;
        }
    }

    //=============================================================================================
    // ##### #   # ####  ####   ###  ##### ##### ####  #      ###  #   # ##### ####  #     #####  ###  ##### ####   ###  #   #
    //   #   #   # #   # #   # #   #   #   #     #   # #     #   #  # #  #     #   # #       #   #       #   #   # #   #  # #
    //   #   #   # ####  #   # #####   #   ####  ####  #     #####   #   ####  ####  #       #    ###    #   ##### #   #   #
    //   #   #   # #     #   # #   #   #   #     #     #     #   #   #   #     #   # #       #       #   #   #   # #   #  # #
    // #####  ###  #     ####  #   #   #   ##### #     ##### #   #   #   ##### #   # ##### #####  ###    #   ####   ###  #   #

    @Override
    public void update()
    {
        if (_forester != null)
            _forester.update();
    }

    // We need this to know to tear down the forester
    public void blockBroken()
    {
        _forester.terminate();
    }

    //=============================================================================================
    // ##### #   # #####  ###   ###   ###   #### ##### ####
    //   #   ## ## #     #     #     #   # #     #     #   #
    //   #   # # # ####   ###   ###  ##### #  ## ####  ####
    //   #   #   # #         #     # #   # #   # #     #   #
    // ##### #   # #####  ###   ###  #   #  #### ##### #   #

    @Override
    public int getRadius()
    {
        return _searchRadius;
    }

    @Override
    public BlockPos getBlockPos()
    {
        return getPos();
    }

    //=============================================================================================

    public int getMaxDroidCount()
    {
        // TODO:  Scale based on area.
        return 2;
    }

    //=============================================================================================

    private class Forester extends AutoBlockBase
    {
        private Forester(World world)
        {
            super(world);
            LOGGER.debug("Constructing: " + this);
        }

        @Override
        protected IMessager getSender()
        {
            return TileEntityAutoForester.this;
        }

        @Override
        protected int concurrentWorkerCount()
        {
            return 3;
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
                    handleWorkerAvailability((MessageWorkerAvailability)msg);
            }
            else if (msg instanceof MessageIsStorageAvailable)
            {
                if (haveFreeWorkerSlots(msg))
                    handleIsStorageAvailable((MessageIsStorageAvailable) msg);
            }
        }
    }

    private void handleItemRequest(MessageItemRequest msg)
    {
        // We only know about logs, leaves, and saplings.
        if (msg.getMatcher().matches(Item.getItemFromBlock(Blocks.sapling)) ||
                msg.getMatcher().matches(Item.getItemFromBlock(Blocks.leaves)) ||
                msg.getMatcher().matches(Item.getItemFromBlock(Blocks.leaves2)) ||
                msg.getMatcher().matches(Item.getItemFromBlock(Blocks.log)) ||
                msg.getMatcher().matches(Item.getItemFromBlock(Blocks.log2)) )
        {
            LOGGER.debug("handleItemRequest: msg=" + msg);

            // If they are looking for saplings and we have empty squares, don't give them out.
            if (msg.getMatcher().matches(Item.getItemFromBlock(Blocks.sapling)))
            {
                if (MALUtils.findEmptyOrchardSquare(getWorld(), getPos(), _searchRadius) != null)
                    return;
            }

            // Look around and see if we have any of these.
            // TODO:  Build better landscape mapper
            BlockPos foundPos = RingedSearcher.findBlock(getWorld(), getPos().down(3), _searchRadius, _searchHeight,
                    msg.getMatcher());
            boolean hasMats = foundPos != null;
            int droidCount = MALUtils.countDroidsInArea(getWorld(), getPos(), getRadius());

            // We only want to respond if we have materials and we aren't already being heavily worked
            if (hasMats && droidCount < getMaxDroidCount())
            {
                // Offer a task, at our area for the requested thing.
                TRHarvest req = new TRHarvest(TileEntityAutoForester.this, msg.getSender(),
                        msg.getTransactionID(), MALMain.CONFIG.getForesterHarvestValue(),
                        TaskHarvestTree.class, msg.getMatcher(), msg.getQuantity());

                debugLog("Forester has item at=" + foundPos + ", for=" + req);
                _broadcastHelper.postMessage(req);
            }

        }
    }

    private void handleWorkerAvailability(MessageWorkerAvailability msg)
    {
        LOGGER.debug("handleWorkerAvailability, this= " + MALUtils.objID(this) +", msg=" + msg);

        //=====================
        // Replant trees

        // TODO:  Build better landscape mapper
        EntityItem pickup = MALUtils.findFirstEntityOfTypeOnGround(getWorld(), getPos(), _searchRadius + 2,
                Item.getItemFromBlock(Blocks.sapling));
        BlockPos target = MALUtils.findEmptyOrchardSquare(getWorld(), getPos(), _searchRadius);

        LOGGER.debug("-- pickup=" + pickup + " , target=" + target);
        if (pickup != null && target != null)
        {
            TRPickup pickupRequest = new TRPickup(TileEntityAutoForester.this,
                    msg.getSender(), msg.getTransactionID(), Priority.getForesterPickupSaplingValue(),
                    -1, Item.getItemFromBlock(Blocks.sapling));

            TRPlantSapling plantRequest = new TRPlantSapling(TileEntityAutoForester.this,
                    msg.getSender(), msg.getTransactionID(), Priority.getForesterPlantSaplingValue());

            MessageTaskPairRequest pairRequest = new MessageTaskPairRequest(TileEntityAutoForester.this,
                    msg.getSender(), msg.getTransactionID(), true, pickupRequest, plantRequest);

//                    //LOGGER.debug("Posting request: " + req);
            _broadcastHelper.postMessage(pairRequest);
            _forester.setNextAvailabilityResponseMS();
            return;
        }

        //=====================

        // Cleanup anything else laying around
        if (MALUtils.generateCleanupTask(this, getWorld(), getBlockPos(), getRadius() + 2, msg))
        {
            _forester.setNextAvailabilityResponseMS();
            return;
        }

        //=====================

        // We can also just provide wood
        ItemStack sample = RingedSearcher.findFirstItemDrop(getWorld(), getPos(), _searchRadius, _searchHeight,
                new ItemTypeMatcher(Item.getItemFromBlock(Blocks.log)));
        if (sample != null)
        {
            // Offer a task, at our area for the thing we found.
            TRHarvest req = new TRHarvest(TileEntityAutoForester.this, msg.getSender(),
                    msg.getTransactionID(), MALMain.CONFIG.getForesterIdleHarvestValue(),
                    TaskHarvestTree.class, new ItemStackMatcher(sample), -1);

            LOGGER.debug("-- posting request: " + req);
            _broadcastHelper.postMessage(req);
            _forester.setNextAvailabilityResponseMS();
        }
    }

    private void handleIsStorageAvailable(MessageIsStorageAvailable msg)
    {
        LOGGER.debug("handleIsStorageAvailable: this="+ MALUtils.objID(this) + " msg=" + msg);

        // We only want saplings.
        if (!isSapling(msg.getMatcher()))
        {
            return;
        }

        BlockPos target = MALUtils.findEmptyOrchardSquare(getWorld(), getPos(), _searchRadius);

        //LOGGER.debug("target=" + target);
        if (target != null)
        {
            TRPlantSapling plantRequest = new TRPlantSapling(TileEntityAutoForester.this,
                    msg.getSender(), msg.getTransactionID(), Priority.getForesterStorageSaplingPlantValue());

            LOGGER.debug("-- posting PLANTING: " + plantRequest);
            _broadcastHelper.postMessage(plantRequest);
        }
    }

    private boolean isSapling(ItemStackMatcher matcher)
    {
        Item sapling = Item.getItemFromBlock(Blocks.sapling);
        return matcher.matches(sapling);
    }


    private void debugLog(String msg)
    {
        LOGGER.debug("Forester: " + msg);
    }

    @Override
    public String toString()
    {
        return "Forester@" + Integer.toHexString(this.hashCode()) + "{}";
    }

    private Forester _forester;
    private Broadcaster.BroadcastHelper _broadcastHelper;
    private int _searchRadius = 5;
    private int _searchHeight = 15;
    private static final Logger LOGGER = LogManager.getLogger();

}
