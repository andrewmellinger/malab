package com.crashbox.drudgemod.ai;

import com.crashbox.drudgemod.EntityDrudge;
import com.crashbox.drudgemod.messaging.Broadcaster;
import com.crashbox.drudgemod.messaging.IListener;
import com.crashbox.drudgemod.messaging.IMessageSender;
import com.crashbox.drudgemod.messaging.Message;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.LinkedTransferQueue;

/**
 * Copyright 2015 Andrew o. Mellinger
 */
public class EntityAIDrudge extends EntityAIBase implements IMessageSender
{
    public EntityAIDrudge(EntityDrudge entity)
    {
        this(entity, DEFAULT_SPEED, DEFAULT_RANGE);
//        Broadcaster.getInstance().subscribe(Broadcaster.Channel.RED, new MyListener());
    }

    public EntityAIDrudge(EntityDrudge entity, double speed, int range)
    {
        this._entity = entity;
    }

    public EntityDrudge getEntity()
    {
        return _entity;
    }

    /**
     * This is called by Taskers when the Worker indicates it may have work.  This
     * must be re-entrant as the worked can be called at any time.   This is expected
     * to be call while we have SYNCHRONOUSLY issued the message available call.
     * @param offer The work offered
     */
    public void offer(TaskBase offer)
    {
        LOGGER.debug("Offer made to bot: " + this);
        _offers.add(offer);
    }

    @Override
    public boolean shouldExecute()
    {
        if (System.currentTimeMillis() < _nextElicit )
        {
            return false;
        }

        _nextElicit = System.currentTimeMillis() + CHECK_DELAY_MILLIS;
        return elicit();
    }

    @Override
    public void startExecuting()
    {
        _currentTask.execute();
    }


    @Override
    public boolean continueExecuting()
    {
        if (!_currentTask.continueExecution())
        {
            _currentTask.complete();
            _currentTask = null;
            return false;
        }
        return true;
    }

    // ================
    // IMessageSender
    @Override
    public int distanceTo(BlockPos pos)
    {
        return 0;
    }

    /**
     * This asks everyone on our communications channel if there is work for us.
     * @return True if we have a new task.
     */
    private boolean elicit()
    {
        LOGGER.debug("Eliciting work: " + this);
        _offers.clear();

        // Send availability message
        // When we return the offers should be full.
        Broadcaster.postMessage(new MessageWorkerAvailability(_entity.worldObj, this), _channel);

        // Process all offers
        TaskBase highestOffer = null;
        int highestWeight = 0;
        TaskBase offer;
        while ((offer = _offers.poll()) != null)
        {
            // Keep the highest around, rejecting all others
            int weight = computeWeight(offer);
            if (weight > highestWeight)
            {
                if (highestOffer != null)
                    highestOffer.reject();

                highestWeight = weight;
                highestOffer = offer;
            }
            else
            {
                LOGGER.debug("Rejecting offer due to weight: " + weight + " <= " + highestWeight);
                offer.reject();
            }
        }

        // If offer then start commuting
        if (highestOffer != null)
        {
            // Accept.
            highestOffer.accept(this);

            // Put into our pending queue so we get executed
            _currentTask = highestOffer;
        }

        // Now that we have processed them all, clear the offers
        _offers.clear();
        return (_currentTask != null);
    }

    // This computes the cost of each task
    // Zero or negative numbers mean we won't do it at all.
    private int computeWeight(TaskBase offer)
    {
        // First come first serve
        return 1;
    }






    //========================
    // PRIVATES
    private EntityDrudge _entity;

    private static final int CHECK_DELAY_MILLIS = 2000;
    private static final int DEFAULT_RANGE = 10;
    private static final double DEFAULT_SPEED = 0.5;

    private long _nextElicit = 0;

    // This is the channel we send and listen on.
    private Broadcaster.Channel _channel = Broadcaster.Channel.RED;

    // Do we have a current task we are pursuing?
    private TaskBase _currentTask;

    // List of tasks tht have been offered to us.
    private final Queue<TaskBase> _offers = new LinkedList<TaskBase>();


    private static final Logger LOGGER = LogManager.getLogger();


}
