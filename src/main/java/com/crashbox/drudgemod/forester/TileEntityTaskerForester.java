package com.crashbox.drudgemod.forester;

import com.crashbox.drudgemod.ai.*;
import com.crashbox.drudgemod.messaging.Message;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class TileEntityTaskerForester extends TileEntity
{
    public static final String NAME = "tileEntityTaskerForester";

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

    private class Forester extends TaskMaster
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
                LOGGER.debug("Forester " + this + " is asked for work. In progress work: " + getInProgress().size());

                // Look around for work
//                BlockPos target = AIUtils.findBlock(getWorld(), getPos(), 10, Blocks.log, getInProgress());

                boolean hasMats = RingedSearcher.findBlock(getWorld(), getPos(), _searchRadius, 10, itemReq.getItemSample());

//                if (target != null)
                if (hasMats)
                {
                    // Offer a task, at our area
//                    TaskBase newOffer = new TaskHarvest(this, target, 0, _searchRadius, 1);
                    TaskBase newOffer = new TaskHarvest(this, getPos(), 0, _searchRadius, itemReq.getQuantity(), itemReq.getItemSample());

                    // Stash off the offers so we can track what we have already offered
                    addTask(newOffer);

                    // Add the offer to the AI
                    itemReq.getAIDrudge().offer(newOffer);
                }
            }
        }
    }

    public void blockBroken()
    {
        _forester.terminate();
    }

    @Override
    public String toString()
    {
        return "TileEntityTaskerForester{" +
                "_forester=" + _forester +
                '}';
    }

    private Forester _forester;
    private int _searchRadius = 5;
    private static final Logger LOGGER = LogManager.getLogger();

}
