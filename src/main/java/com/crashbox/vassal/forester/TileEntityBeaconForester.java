package com.crashbox.vassal.forester;

import com.crashbox.vassal.VassalUtils;
import com.crashbox.vassal.ai.*;
import com.crashbox.vassal.beacon.BeaconBase;
import com.crashbox.vassal.common.ItemStackMatcher;
import com.crashbox.vassal.common.ItemTypeMatcher;
import com.crashbox.vassal.messaging.*;
import com.crashbox.vassal.task.TaskHarvestTree;
import com.crashbox.vassal.util.RingedSearcher;
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
public class TileEntityBeaconForester extends TileEntity implements IUpdatePlayerListBox, IMessager
{
    public static final String NAME = "tileEntityBeaconForester";

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

    public int getMaxVassalCount()
    {
        // TODO:  Scale based on area.
        return 2;
    }

    //=============================================================================================

    private class Forester extends BeaconBase
    {
        private Forester(World world)
        {
            super(world);
            LOGGER.debug("Constructing: " + this);
        }

        @Override
        protected IMessager getSender()
        {
            return TileEntityBeaconForester.this;
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
                if (haveFreeWorkerSlots())
                    handleItemRequest((MessageItemRequest) msg);
            }
            else if (msg instanceof MessageWorkerAvailability && haveFreeWorkerSlots())
            {
                if (haveFreeWorkerSlots())
                    handleWorkerAvailability((MessageWorkerAvailability)msg);
            }
            else if (msg instanceof MessageIsStorageAvailable)
            {
                if (haveFreeWorkerSlots())
                    handleIsStorageAvailable((MessageIsStorageAvailable) msg);
            }
        }
    }

    private void handleItemRequest(MessageItemRequest msg)
    {
        debugLog("Asked for items." + msg);

        MessageItemRequest itemReq = msg;

        // Look around and see if we have any of these.
        boolean hasMats = RingedSearcher.detectBlock(getWorld(), getPos(), _searchRadius, _searchHeight,
                itemReq.getMatcher());
        int vassalCount = VassalUtils.countVassalsInArea(getWorld(), getPos(), getRadius());

        // We only want to respond if we have materials and we aren't already being heavily worked
        if (hasMats && vassalCount < getMaxVassalCount())
        {
            // Offer a task, at our area for the requested thing.
            TRHarvest req = new TRHarvest(TileEntityBeaconForester.this, itemReq.getSender(),
                    msg.getTransactionID(), Priority.getForesterHarvestValue(),
                    TaskHarvestTree.class, itemReq.getMatcher(), itemReq.getQuantity());

            debugLog("Posting request: " + req);
            Broadcaster.postMessage(req);
        }
    }

    private void handleWorkerAvailability(MessageWorkerAvailability msg)
    {
        //LOGGER.debug("Forester " + this + " is asked for work." + msg);

        //=====================

        EntityItem pickup = VassalUtils.findFirstEntityOfTypeOnGround(getWorld(), getPos(), _searchRadius,
                Item.getItemFromBlock(Blocks.sapling));
        BlockPos target = VassalUtils.findEmptyOrchardSquare(getWorld(), getPos(), _searchRadius);

        LOGGER.debug("pickup=" + pickup + " , target=" + target);
        if (pickup != null && target != null)
        {
            TRPickup pickupRequest = new TRPickup(TileEntityBeaconForester.this,
                    msg.getSender(), msg.getTransactionID(), Priority.getForesterPickupSaplingValue(),
                    4, Item.getItemFromBlock(Blocks.sapling));

            TRPlantSapling plantRequest = new TRPlantSapling(TileEntityBeaconForester.this,
                    msg.getSender(), msg.getTransactionID(), Priority.getForesterPlantSaplingValue());

            MessageTaskPairRequest pairRequest = new MessageTaskPairRequest(TileEntityBeaconForester.this,
                    msg.getSender(), msg.getTransactionID(), true, pickupRequest, plantRequest);

//                    //LOGGER.debug("Posting request: " + req);
            Broadcaster.postMessage(pairRequest);
            return;
        }

        //=====================

        if (pickup != null)
        {
            if (VassalUtils.generateCleanupTask(this, getWorld(), getBlockPos(), getRadius(),msg))
                return;
        }

        // We can also just provide wood

//        boolean hasMats = RingedSearcher.detectBlock(getWorld(), getPos(), _searchRadius, _searchHeight,
//                new ItemTypeMatcher(Item.getItemFromBlock(Blocks.log)));

        //int vassalCount = AIUtils.countVassalsInArea(getWorld(), getPos(), getRadius());
        ItemStack sample = RingedSearcher.findFirstItemDrop(getWorld(), getPos(), _searchRadius, _searchHeight,
                new ItemTypeMatcher(Item.getItemFromBlock(Blocks.log)));

        // We only want to respond if we have materials and we aren't already being heavily worked
        if (sample != null)
        {
            // Offer a task, at our area for the requested thing.
            TRHarvest req = new TRHarvest(TileEntityBeaconForester.this, msg.getSender(),
                    msg.getTransactionID(), Priority.getForesterIdleHarvestingValue(),
                    TaskHarvestTree.class, new ItemStackMatcher(sample), -1);

            debugLog("Posting request: " + req);
            Broadcaster.postMessage(req);
        }
    }

    private void handleIsStorageAvailable(MessageIsStorageAvailable msg)
    {
        //LOGGER.debug("Forester " + this + " is asked for work." + msg);

        // We only want saplings.
        LOGGER.debug("Forester storing: " + msg);
        if (!isSapling(msg.getMatcher()))
        {
            LOGGER.debug("NOT A SAPLING");
            return;
        }

        BlockPos target = VassalUtils.findEmptyOrchardSquare(getWorld(), getPos(), _searchRadius);

        //LOGGER.debug("target=" + target);
        if (target != null)
        {
            TRPlantSapling plantRequest = new TRPlantSapling(TileEntityBeaconForester.this,
                    msg.getSender(), msg.getTransactionID(), Priority.getForesterStorageSaplingPlantValue());

            LOGGER.debug("Posting PLANTING: " + plantRequest);
            Broadcaster.postMessage(plantRequest);
        }
    }

    private boolean isSapling(ItemStackMatcher matcher)
    {
        Item sapling = Item.getItemFromBlock(Blocks.sapling);

        LOGGER.debug(matcher);
        LOGGER.debug(sapling);

        return matcher.matches(sapling);
    }


    private void debugLog(String msg)
    {
        LOGGER.debug("Forester: " + msg);
    }


    @Override
    public String toString()
    {
        return "Forester{" +
                "_forester=" + _forester +
                '}';
    }

    private Forester _forester;
    private int _searchRadius = 5;
    private int _searchHeight = 10;
    private static final Logger LOGGER = LogManager.getLogger();

}
