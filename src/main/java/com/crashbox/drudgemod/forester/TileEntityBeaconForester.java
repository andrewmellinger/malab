package com.crashbox.drudgemod.forester;

import com.crashbox.drudgemod.ai.*;
import com.crashbox.drudgemod.beacon.BeaconBase;
import com.crashbox.drudgemod.messaging.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
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
                //LOGGER.debug("Forester " + this + " is asked for items." + msg);

                MessageItemRequest itemReq = (MessageItemRequest)msg;

                // Look around and see if we have any of these.
                boolean hasMats = RingedSearcher.detectBlock(getWorld(), getPos(), _searchRadius, _searchHeight,
                        itemReq.getMatcher());
                if (hasMats)
                {
                    // Offer a task, at our area for the requested thing.
                    MessageHarvestRequest req = new MessageHarvestRequest(TileEntityBeaconForester.this, itemReq.getSender(),
                            msg.getTransactionID(), 0, itemReq.getMatcher(), itemReq.getQuantity());

                    //LOGGER.debug("Posting request: " + req);
                    Broadcaster.postMessage(req);
                }
            }
            else if (msg instanceof MessageWorkerAvailability && timeForAvailabilityResponse())
            {
                //LOGGER.debug("Forester " + this + " is asked for work." + msg);

                MessageWorkerAvailability availability = (MessageWorkerAvailability)msg;

                EntityItem pickup = AIUtils.findFirstEntityOfTypeOnGround(getWorld(), getPos(), _searchRadius,
                        Item.getItemFromBlock(Blocks.sapling));
                BlockPos target = AIUtils.findEmptyOrchardSquare(getWorld(), getPos(), _searchRadius);

                //LOGGER.debug("pickup=" + pickup + " , target=" + target);

                if (pickup != null && target != null)
                {
                    MessagePlantSaplings req = new MessagePlantSaplings(TileEntityBeaconForester.this,
                            availability.getSender(), msg.getTransactionID(), 0);

                    //LOGGER.debug("Posting request: " + req);
                    Broadcaster.postMessage(req);
                }
            }
        }
    }


    @Override
    public String toString()
    {
        return "TileEntityBeaconForester{" +
                "_forester=" + _forester +
                '}';
    }

    private Forester _forester;
    private int _searchRadius = 5;
    private int _searchHeight = 10;
    private static final Logger LOGGER = LogManager.getLogger();

}
