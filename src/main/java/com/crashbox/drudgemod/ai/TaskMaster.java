package com.crashbox.drudgemod.ai;

import com.crashbox.drudgemod.messaging.Broadcaster;
import com.crashbox.drudgemod.messaging.IListener;
import com.crashbox.drudgemod.messaging.Message;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public abstract class TaskMaster
{
    protected TaskMaster(World world)
    {
        // We only want to listen on the server
        if (!world.isRemote)
        {
            // TODO:  Deal with channel changes
            LOGGER.debug("****************************************************************************");
            LOGGER.debug("************************************************* TaskMaster BEFORE listener");
            _listener = new Listener();
            LOGGER.debug("TaskMaster AFTER listener");
            Broadcaster.getInstance().subscribe(_inChannel, _listener);
            LOGGER.debug("TaskMaster AFTER subscribe");
        }
    }

    /**
     * Called to cleanup then listener
     */
    public void terminate()
    {
        if (_listener != null)
        {
            Broadcaster.getInstance().unsubscribe(_inChannel, _listener);
            _listener = null;
        }
    }

    /**
     * Extension point for taskers to see if they have approprite work.
     * They should check to see if they have work.
     * @param msg The message indicating the worker is ready.
     */
    protected abstract void checkWork(MessageWorkerAvailability msg);


    // =================
    // Progress track

    // Called by the Task when it is rejected
    public void taskRejected(TaskBase task)
    {
        LOGGER.debug("REJECTED: Removing task from offer tracker: " + task);
        _inProgress.remove(task);
    }

    // Called by the Task when it is completed
    public void taskCompleted(TaskBase task)
    {
        LOGGER.debug("COMPLETED: Removing task from offer tracker: " + task);
        _inProgress.remove(task);
    }

    // Called by subclasses to see what tasks there are
    protected List<TaskBase> getInProgress()
    {
        return _inProgress;
    }

    // Called by subclass to add a task to the list
    protected void addTask(TaskBase task)
    {
        LOGGER.debug("ADD_OFFER: Adding task to offer tracker: " + task);
        _inProgress.add(task);
    }

    // Here we track made offers.
    private List<TaskBase> _inProgress = new ArrayList<TaskBase>();


    // Listener to deal with all the incoming messages
    private class Listener implements IListener
    {
        @Override
        public void handleMessage(Message message)
        {
            if (message instanceof MessageWorkerAvailability)
            {
                checkWork((MessageWorkerAvailability)message);
            }
        }
    }

    @Override
    public String toString()
    {
        return "TaskMaster{" +
                "_inProgress=" + _inProgress.size() +
                ", _listener=" + _listener +
                ", _inChannel=" + _inChannel +
                '}';
    }

    private Listener _listener;
    private Broadcaster.Channel _inChannel = Broadcaster.Channel.RED;

    private static final Logger LOGGER = LogManager.getLogger();

}
