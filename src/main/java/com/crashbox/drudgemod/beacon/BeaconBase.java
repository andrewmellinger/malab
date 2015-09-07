package com.crashbox.drudgemod.beacon;

import com.crashbox.drudgemod.DrudgeUtils;
import com.crashbox.drudgemod.messaging.*;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Queue;
import java.util.Random;
import java.util.concurrent.LinkedTransferQueue;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public abstract class BeaconBase
{
    protected BeaconBase(World world)
    {
        Random rand = new Random();
        // We only want to listen on the server
        if (!world.isRemote)
        {
            // TODO:  Deal with channel changes
            _listener = new Listener();
            Broadcaster.getInstance().subscribe(_inChannel, _listener);
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
     * Should be called by the owner of this object to process all the messages.
     * For example, in the TileEntity.
     */
    public void update()
    {
        Message msg;
        while (( msg = _messages.poll()) != null)
        {
            if (msg instanceof MessageWorkAccepted)
            {
                if (msg.getTarget() == getSender())
                {
                    //LOGGER.debug(id() + " got accepted work message.: " + msg);
                    handleWorkAccepted((MessageWorkAccepted) msg);
                }
                continue;
            }

            // Let them handle it
            handleMessage(msg);
        }
    }

    /**
     * @return The object that sends messages.  This is usually a tile entity or drudge.
     */
    protected abstract IMessager getSender();

    /**
     * Extension point for beacon to see if they have appropriate work.
     * They should check to see if they have work.
     * @param msg The message indicating the worker is ready.
     */
    protected abstract void handleMessage(Message msg);

    /**
     * @return The channel we are attached to.
     */
    public Broadcaster.Channel getChannel()
    {
        return _inChannel;
    }

    //=============================================================================================

    protected void handleWorkAccepted(MessageWorkAccepted msg)
    {
        _nextAvailabilityResponseMS = System.currentTimeMillis() + msg.getDelayMS();
    }

    protected boolean timeForAvailabilityResponse()
    {
        return System.currentTimeMillis() > _nextAvailabilityResponseMS;
    }

    //=============================================================================================

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
        return "Beacon{" +
                ", _listener=" + Integer.toHexString(_listener.hashCode()) +
                ", _inChannel=" + _inChannel +
                '}';
    }

    public String id()
    {
        return DrudgeUtils.objID(this);
    }


    private final Queue<Message> _messages = new LinkedTransferQueue<Message>();

    private Listener _listener;
    private Broadcaster.Channel _inChannel = Broadcaster.Channel.RED;

    // We only want to respond everyone once in a while.  They send us a message
    // when they accept
    private long _nextAvailabilityResponseMS = 0;


    private static final Logger LOGGER = LogManager.getLogger();

}
