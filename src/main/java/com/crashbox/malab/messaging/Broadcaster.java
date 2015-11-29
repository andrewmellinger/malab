package com.crashbox.malab.messaging;

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

    public static void postMessage(Message message, int dimensionID)
    {
        getInstance().broadcastSync(message, dimensionID);
    }

    public void subscribe(IListener listener, int dimensionId)
    {
        synchronized (_lock)
        {
            List<IListener> listeners = _listeners.get(dimensionId);
            if (listeners == null)
            {
                listeners = new ArrayList<IListener>();
                _listeners.put(dimensionId, listeners);
            }

            if (!listeners.contains(listener))
                listeners.add(listener);
        }
    }

    public void unsubscribe(IListener listener)
    {
        synchronized (_lock)
        {
            for (List<IListener> listeners : _listeners.values())
            {
                listeners.remove(listener);
            }
        }
    }

    /**
     * This is synchronous!
     * @param message The message to send.
     * @param dimensionId The dimension we are targeting
     */
    public void broadcastSync(Message message, int dimensionId)
    {
        List<IListener> listeners;
        synchronized (_lock)
        {
            List<IListener> tmp = _listeners.get(dimensionId);
            if (tmp == null)
                return;
            listeners = new ArrayList<IListener>(tmp);
        }

        for (IListener tmp : listeners)
        {
            tmp.handleMessage(message);
        }
    }

    public static class BroadcastHelper
    {
        public BroadcastHelper(int dimensionId)
        {
            _dimensionId = dimensionId;
            _broadcaster = Broadcaster.getInstance();
        }

        public void postMessage(Message message)
        {
            _broadcaster.broadcastSync(message, _dimensionId);
        }

        private Broadcaster _broadcaster;
        private final int _dimensionId;
    }

    //-------------------------------------------------------------------------

    // TODO: Make listener list weak
    private final Object _lock = new Object();

    private Map<Integer, List<IListener>> _listeners = new HashMap<Integer, List<IListener>>();
    private static Broadcaster BROADCASTER;

    private static final Logger LOGGER = LogManager.getLogger();
}
