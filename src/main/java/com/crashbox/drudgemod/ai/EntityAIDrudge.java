package com.crashbox.drudgemod.ai;

import com.crashbox.drudgemod.EntityDrudge;
import com.crashbox.drudgemod.messaging.Broadcaster;
import com.crashbox.drudgemod.messaging.IMessageSender;
import com.crashbox.drudgemod.messaging.Message;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Copyright 2015 Andrew O. Mellinger
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

    private TaskBase elicit(Message msg)
    {
        LOGGER.debug("Eliciting: " + msg);
        Queue<TaskBase> previous = new LinkedList<TaskBase>(_offers);
        _offers.clear();

        // Post message
        Broadcaster.postMessage(msg, _channel);

        // If we didn't get any new offers we are done
        if (_offers.size() == 0)
        {
            LOGGER.debug("No offers made.");
            _offers.addAll(previous);
            return null;
        }

        // Find highest offer, and see if we can do it.
        TaskBase highest;
        while ((highest = findHighest(_offers)) != null)
        {
            LOGGER.debug("Processing highest: " + highest);
            if (!canPerform(highest))
            {
                LOGGER.debug("Can't perform task, rejecting: " + highest);
                highest.reject();
                continue;
            }

            Message preReq = checkPrerequisite(highest);
            if (preReq != null)
            {
                LOGGER.debug("Has pre-req: " + preReq);
                TaskBase preReqTask = elicit(preReq);
                if (preReqTask == null)
                {
                    // Couldn't find a task for the pre-req
                    LOGGER.debug("Couldn't find task for pre-req rejecting.");
                    highest.reject();
                }
                else
                {
                    preReqTask.setNextTask(highest);
                    highest = preReqTask;
                    rejectOffers();
                    _offers.addAll(previous);
                    return highest;
                }
            }
            else
            {
                return highest;
            }
        }

        // Ran out of offers
        LOGGER.debug("Ran out of offers.");

        _offers.addAll(previous);
        return null;
    }

    private boolean canPerform(TaskBase task)
    {
        if (task instanceof TaskCarryTo)
        {
            TaskCarryTo deliver = (TaskCarryTo)task;
            ItemStack held = getEntity().getHeldItem();
            if (held == null || held.getItem() == deliver.getItemType())
            {
                return true;
            }
        }
        if (task instanceof TaskHarvest)
        {
            TaskHarvest harvest = (TaskHarvest)task;
            return true;
        }
        return false;
    }

    private Message checkPrerequisite(TaskBase task)
    {
        if (task instanceof TaskCarryTo)
        {
            TaskCarryTo deliver = (TaskCarryTo)task;
            if (getEntity().getHeldItem() == null)
            {
                return new MessageItemRequest(this, deliver.getItemType(), deliver.getQuantity());
            }
        }
        return null;
    }

    private void rejectOffers()
    {
        TaskBase task;
        while ((task = _offers.poll()) != null)
        {
            task.reject();
        }
    }

    private TaskBase findHighest(Queue<TaskBase> list)
    {
        return list.poll();
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
        TaskBase task = elicit(new MessageWorkerAvailability(_entity.worldObj, this));

        // If we have one, accept all
        if (task != null)
        {
            _currentTask = task;
            task.accept(this);
            while ((task = task.getNextTask()) != null)
                task.accept(this);
            _offers.clear();
            return true;
        }
        _offers.clear();
        return false;
    }

    // This computes the cost of each task
    // Zero or negative numbers mean we won't do it at all.
    private int computeWeight(TaskBase offer)
    {
        // First come first serve
        return 1;
    }

    @Override
    public String toString()
    {
        return "EntityAIDrudge{" +
//                "_entity=" + _entity +
//                ", _nextElicit=" + _nextElicit +
//                ", _channel=" + _channel +
//                ", _currentTask=" + _currentTask +
//                ", _offers=" + _offers +
                '}';
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
