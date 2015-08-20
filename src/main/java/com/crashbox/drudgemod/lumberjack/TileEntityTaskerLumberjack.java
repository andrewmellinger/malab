package com.crashbox.drudgemod.lumberjack;

import com.crashbox.drudgemod.ai.*;
import com.crashbox.drudgemod.messaging.Message;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class TileEntityTaskerLumberjack  extends TileEntity
{
    public static final String NAME = "tileEntityTaskerLumberjack";

    @Override
    public void setWorldObj(World worldIn)
    {
        LOGGER.debug("setWorldObj: " + worldIn);
        super.setWorldObj(worldIn);
        if (worldIn != null && !worldIn.isRemote)
        {
            _lumberjack = new Lumberjack(worldIn);
        }
        else
        {
            if (_lumberjack != null)
                _lumberjack.terminate();
            _lumberjack = null;
        }
    }

    private class Lumberjack extends TaskMaster
    {
        private Lumberjack(World world)
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
                LOGGER.debug("Lumberjack " + this + " is asked for work. In progress work: " + getInProgress().size());

                // Look around for work
                BlockPos target = AIUtils.findBlock(getWorld(), getPos(), 10, Blocks.log, getInProgress());

                if (target != null)
                {
                    LOGGER.debug("********* Found log at: " + target);

                    // Offer a task
                    TaskBase newOffer = new TaskHarvest(this, target, 0);

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
        _lumberjack.terminate();
    }

    @Override
    public String toString()
    {
        return "TileEntityTaskerLumberjack{" +
                "_lumberjack=" + _lumberjack +
                '}';
    }

    private Lumberjack _lumberjack;
    private static final Logger LOGGER = LogManager.getLogger();

}
