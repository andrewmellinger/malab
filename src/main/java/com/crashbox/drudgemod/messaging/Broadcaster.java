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

    public static void postMessage(Message message, Channel channel)
    {
        getInstance().broadcastSync(message, channel);
    }

    // Numbers
    public enum Channel { BLACK, WHITE, RED, GREEN, BLUE }

    public void subscribe(Channel channel, IListener listener)
    {
        synchronized (_lock)
        {
            if (!_queue.containsKey(channel))
            {
                _queue.put(channel, new ArrayList<IListener>());
            }

            List<IListener> listenerList = _queue.get(channel);

            // We don't like dupes
            boolean foundIt = false;
            for (IListener tmp : listenerList)
            {
                if (tmp == listener)
                {
                    foundIt = true;
                    break;
                }
            }

            if (!foundIt)
            {
                listenerList.add(listener);
            }
        }
    }

    public void unsubscribe(Channel channel, IListener listener)
    {
        synchronized (_lock)
        {
            List<IListener> listenerList = _queue.get(channel);
            listenerList.remove(listener);
        }
    }

    /**
     * This is synchronous!
     * @param message The message to send.
     * @param channel The channel on which to send.
     */
    public void broadcastSync(Message message, Channel channel)
    {
        List<IListener> listenerList = null;
        synchronized (_lock)
        {
            if (_queue.containsKey(channel))
            {
                listenerList = new ArrayList<IListener>(_queue.get(channel));
            }
        }
        if (listenerList != null)
        {
            for (IListener tmp : listenerList)
            {
                tmp.handleMessage(message);
            }
        }
    }

    //-------------------------------------------------------------------------

    // TODO: Make listener list weak
    private final Object _lock = new Object();
    private final Map<Channel, List<IListener>> _queue = new HashMap<Channel, List<IListener>>();

    private static Broadcaster BROADCASTER;

    private static final Logger LOGGER = LogManager.getLogger();
}
