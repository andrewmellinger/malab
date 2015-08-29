package com.crashbox.drudgemod.forester;

import com.crashbox.drudgemod.ai.*;
import com.crashbox.drudgemod.beacon.BeaconBase;
import com.crashbox.drudgemod.messaging.Message;
import com.crashbox.drudgemod.messaging.MessageItemRequest;
import com.crashbox.drudgemod.messaging.MessageWorkerAvailability;
import com.crashbox.drudgemod.task.TaskBase;
import com.crashbox.drudgemod.task.TaskHarvest;
import com.crashbox.drudgemod.task.TaskPlantSapling;
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
public class TileEntityBeaconForester extends TileEntity implements IUpdatePlayerListBox
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



    private class Forester extends BeaconBase
    {
        private Forester(World world)
        {
            super(world);
            LOGGER.debug("Constructing: " + this);
        }

        @Override
        protected void handleMessage(Message msg)
        {
            if (msg instanceof MessageItemRequest)
            {
                MessageItemRequest itemReq = (MessageItemRequest)msg;
                LOGGER.debug("Forester " + this + " is asked for work.");

                // Look around for work
//                BlockPos target = AIUtils.findBlock(getWorld(), getPos(), 10, Blocks.log, getInProgress());

                boolean hasMats = RingedSearcher.findBlock(getWorld(), getPos(), _searchRadius, _searchHeight, itemReq.getItemSample());

//                if (target != null)
                if (hasMats)
                {
                    // Offer a task, at our area
//                    TaskBase newOffer = new TaskHarvest(this, target, 0, _searchRadius, 1);
                    TaskBase newOffer = new TaskHarvest(this, getPos(), 0, _searchRadius, itemReq.getQuantity(), itemReq.getItemSample());

                    // Stash off the offers so we can track what we have already offered











//                    addTask(newOffer);

                    // Add the offer to the AI
                    itemReq.getAIDrudge().offer(newOffer);
                }
            }
            else if (msg instanceof MessageWorkerAvailability)
            {
                MessageWorkerAvailability availability = (MessageWorkerAvailability)msg;

                EntityItem pickup = AIUtils.findFirstEntityOfTypeOnGround(getWorld(), getPos(), _searchRadius,
                        Item.getItemFromBlock(Blocks.sapling));
                BlockPos target = AIUtils.findEmptyOrchardSquare(getWorld(), getPos(), _searchRadius);

                if (pickup != null && target != null)
                {
                    TaskBase task = new TaskPlantSapling(this, getPos(), 0, _searchRadius);
                    LOGGER.debug("Posting task: " + task);
                    availability.getAIDrudge().offer(task);
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
