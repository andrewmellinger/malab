package com.crashbox.drudgemod.forester;

import com.crashbox.drudgemod.ai.*;
import com.crashbox.drudgemod.beacon.BeaconBase;
import com.crashbox.drudgemod.common.ItemStackMatcher;
import com.crashbox.drudgemod.common.ItemTypeMatcher;
import com.crashbox.drudgemod.messaging.*;
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

    @Override
    public int getRadius()
    {
        return _searchRadius;
    }

    public int getMaxDrudgeCount()
    {
        // TODO:  Scale based on area.
        return 2;
    }

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
        protected void handleMessage(Message msg)
        {
            if (msg instanceof MessageItemRequest)
            {
                handleItemRequest((MessageItemRequest) msg);
            }
            else if (msg instanceof MessageWorkerAvailability && timeForAvailabilityResponse())
            {
                handleWorkerAvailability((MessageWorkerAvailability)msg);
            }
            else if (msg instanceof MessageIsStorageAvailable)
            {
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
        int drudgeCount = AIUtils.countDrudgesInArea(getWorld(), getPos(), getRadius());

        // We only want to respond if we have materials and we aren't already being heavily worked
        if (hasMats && drudgeCount < getMaxDrudgeCount())
        {
            // Offer a task, at our area for the requested thing.
            TRHarvest req = new TRHarvest(TileEntityBeaconForester.this, itemReq.getSender(),
                    msg.getTransactionID(), 10, itemReq.getMatcher(), itemReq.getQuantity());

            debugLog("Posting request: " + req);
            Broadcaster.postMessage(req);
        }
    }

    private void handleWorkerAvailability(MessageWorkerAvailability msg)
    {
        //LOGGER.debug("Forester " + this + " is asked for work." + msg);

        MessageWorkerAvailability availability = (MessageWorkerAvailability)msg;

        //=====================

        EntityItem pickup = AIUtils.findFirstEntityOfTypeOnGround(getWorld(), getPos(), _searchRadius,
                Item.getItemFromBlock(Blocks.sapling));
        BlockPos target = AIUtils.findEmptyOrchardSquare(getWorld(), getPos(), _searchRadius);

        LOGGER.debug("pickup=" + pickup + " , target=" + target);
        if (pickup != null && target != null)
        {
            TRPickup pickupRequest = new TRPickup(TileEntityBeaconForester.this,
                    msg.getSender(), msg.getTransactionID(), 0, 4, Item.getItemFromBlock(Blocks.sapling));

            TRPlantSapling plantRequest = new TRPlantSapling(TileEntityBeaconForester.this,
                    msg.getSender(), msg.getTransactionID(), 0);

            MessageTaskPairRequest pairRequest = new MessageTaskPairRequest(TileEntityBeaconForester.this,
                    msg.getSender(), msg.getTransactionID(), true, pickupRequest, plantRequest);

//                    //LOGGER.debug("Posting request: " + req);
            Broadcaster.postMessage(pairRequest);
        }

        //=====================

        // We can also just provide wood

//        boolean hasMats = RingedSearcher.detectBlock(getWorld(), getPos(), _searchRadius, _searchHeight,
//                new ItemTypeMatcher(Item.getItemFromBlock(Blocks.log)));

        //int drudgeCount = AIUtils.countDrudgesInArea(getWorld(), getPos(), getRadius());

        ItemStack sample = RingedSearcher.findFirstItemDrop(getWorld(), getPos(), _searchRadius, _searchHeight,
                new ItemTypeMatcher(Item.getItemFromBlock(Blocks.log)));

        // We only want to respond if we have materials and we aren't already being heavily worked
        if (sample != null)
        {
            // Offer a task, at our area for the requested thing.
            TRHarvest req = new TRHarvest(TileEntityBeaconForester.this, availability.getSender(),
                    msg.getTransactionID(), 10, new ItemStackMatcher(sample), -1);

            debugLog("Posting request: " + req);
            Broadcaster.postMessage(req);
        }
    }

    private void handleIsStorageAvailable(MessageIsStorageAvailable msg)
    {
        //LOGGER.debug("Forester " + this + " is asked for work." + msg);

        MessageIsStorageAvailable storage = (MessageIsStorageAvailable)msg;

        // We only want saplings.
        LOGGER.debug(storage);
        if (!isSapling(storage.getMatcher()))
        {
            return;
        }

        BlockPos target = AIUtils.findEmptyOrchardSquare(getWorld(), getPos(), _searchRadius);

        //LOGGER.debug("target=" + target);
        if (target != null)
        {
            TRPlantSapling plantRequest = new TRPlantSapling(TileEntityBeaconForester.this,
                    msg.getSender(), msg.getTransactionID(), 0);

//                    //LOGGER.debug("Posting request: " + req);
            Broadcaster.postMessage(plantRequest);
        }
    }

    private boolean isSapling(ItemStackMatcher matcher)
    {
        //List<ItemStack> stacks = OreDictionary.getOres("log");

        Item log1 = Item.getItemFromBlock(Blocks.log);
        Item log2 = Item.getItemFromBlock(Blocks.log2);

        return matcher.matches(new ItemStack(log1)) || matcher.matches(new ItemStack(log2));
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
