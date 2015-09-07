package com.crashbox.drudgemod.messaging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class Broadcaster
{
    public static Broadcaster getInstance()
    {
        if (BROADCASTER == null)
        {
            LOGGER.debug("Created new broadcaster");
            BROADCASTER = new Broadcaster();
        }
        return BROADCASTER;
    }

    public static void postMessage(Message message)
    {
        getInstance().broadcastSync(message);
    }

    public void subscribe(IListener listener)
    {
        synchronized (_lock)
        {
            if (!_listeners.contains(listener))
                _listeners.add(listener);
        }
    }

    public void unsubscribe(IListener listener)
    {
        synchronized (_lock)
        {
            _listeners.remove(listener);
        }
    }

    /**
     * This is synchronous!
     * @param message The message to send.
     */
    public void broadcastSync(Message message)
    {
        for (IListener tmp : _listeners)
        {
            tmp.handleMessage(message);
        }
    }

    //-------------------------------------------------------------------------

    // TODO: Make listener list weak
    private final Object _lock = new Object();
    private final List<IListener> _listeners = new ArrayList<IListener>();

    private static Broadcaster BROADCASTER;

    private static final Logger LOGGER = LogManager.getLogger();
}
