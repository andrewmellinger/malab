package com.crashbox.drudgemod.beacon;

import com.crashbox.drudgemod.messaging.Broadcaster;
import com.crashbox.drudgemod.messaging.IListener;
import com.crashbox.drudgemod.messaging.Message;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedTransferQueue;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public abstract class BeaconBase
{
    protected BeaconBase(World world)
    {
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
     * Should be called by the owne of this object to process all the messages.
     * For example, in the TileEntity.
     */
    public void update()
    {
        Message msg;
        while (( msg = _messages.poll()) != null)
        {
            handleMessage(msg);
        }
    }

    /**
     * Extension point for beacon to see if they have appropriate work.
     * They should check to see if they have work.
     * @param msg The message indicating the worker is ready.
     */
    protected abstract void handleMessage(Message msg);

    /**
     * @return The channe we are attached to.
     */
    public Broadcaster.Channel getChannel()
    {
        return _inChannel;
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


    private final Queue<Message> _messages = new LinkedTransferQueue<Message>();

    private Listener _listener;
    private Broadcaster.Channel _inChannel = Broadcaster.Channel.RED;

    private static final Logger LOGGER = LogManager.getLogger();

}
