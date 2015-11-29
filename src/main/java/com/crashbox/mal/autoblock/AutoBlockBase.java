package com.crashbox.mal.autoblock;

import com.crashbox.mal.ai.Priority;
import com.crashbox.mal.util.MALUtils;
import com.crashbox.mal.messaging.*;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedTransferQueue;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public abstract class AutoBlockBase
{
    protected AutoBlockBase(World world)
    {
        // We only want to listen on the server
        if (!world.isRemote)
        {
            _listener = new Listener();
            Broadcaster.getInstance().subscribe(_listener, world.provider.getDimensionId());
        }
    }

    /**
     * Called to cleanup then listener
     */
    public void terminate()
    {
        if (_listener != null)
        {
            Broadcaster.getInstance().unsubscribe(_listener);
            _listener = null;
        }
    }

    /**
     * Should be called by the owner of this object to process all the messages.
     * For example, in the TileEntity.
     */
    public void update()
    {
        Message msg;
        while (( msg = _messages.poll()) != null)
        {
            if (msg instanceof MessageWorkingHeartbeat)
            {
                if (msg.getTarget() == getSender())
                {
                    //LOGGER.debug(id() + " got accepted work message.: " + msg);
                    handleWorkHeartbeat((MessageWorkingHeartbeat) msg);
                }
                continue;
            }

            if (msg.getTarget() == null || msg.getTarget() == getSender())
                handleMessage(msg);
        }
    }

    /**
     * @return The object that sends messages.  This is usually a tile entity or work droid.
     */
    protected abstract IMessager getSender();

    /**
     * Extension point for block to see if they have appropriate work.
     * They should check to see if they have work.
     * @param msg The message indicating the worker is ready.
     */
    protected abstract void handleMessage(Message msg);

    /**
     * @return Number of concurrent workers we allow.
     */
    protected abstract int concurrentWorkerCount();

    //=============================================================================================

    protected void handleWorkHeartbeat(MessageWorkingHeartbeat msg)
    {
        //LOGGER.debug("Got heartbeat for: " + msg.getSender());
        _lastHeartbeat.put(msg.getSender(), msg.getExpireMS());
    }

    protected boolean haveFreeWorkerSlots(Message msg)
    {
        // Prune the list
        ageOutWorkerList();

        if (_lastHeartbeat.containsKey(msg.getSender()))
            return true;
        return _lastHeartbeat.size() < concurrentWorkerCount();
    }

    //=============================================================================================

    private void ageOutWorkerList()
    {
        long now = System.currentTimeMillis();

        Iterator<Map.Entry<IMessager, Long>> iter = _lastHeartbeat.entrySet().iterator();
        while (iter.hasNext())
        {
            Map.Entry<IMessager, Long> next =  iter.next();
            if (now > next.getValue())
            {
                iter.remove();
            }
        }
    }

    // Listener to deal with all the incoming messages
    private class Listener implements IListener
    {
        @Override
        public void handleMessage(Message message)
        {
            _messages.add(message);
        }
    }

    @Override
    public String toString()
    {
        String listener = "none";
        if (_listener != null)
            listener = Integer.toHexString(_listener.hashCode());

        return "AutoBlock{" +
                ", _listener=" + listener +
                '}';
    }

    public String id()
    {
        return MALUtils.objID(this);
    }

    public boolean readyForNextAvailabilityResponseMS()
    {
        return _nextAvailabilityResponseMS < System.currentTimeMillis();
    }

    public void setNextAvailabilityResponseMS()
    {
        _nextAvailabilityResponseMS = System.currentTimeMillis() + Priority.AVAILABILITY_RESPONSE_DELAY_MS;
    }

    private final Queue<Message> _messages = new LinkedTransferQueue<Message>();
    private long _nextAvailabilityResponseMS = 0;

    private Listener _listener;

    // A list of the people we have working on our project.
    private final Map<IMessager, Long> _lastHeartbeat = new HashMap<IMessager, Long>();

}
