package com.crashbox.drudgemod.ai;

import com.crashbox.drudgemod.messaging.Message;
import com.crashbox.drudgemod.messaging.IMessageSender;
import net.minecraft.world.World;

/**
 * Copyright 2015 Andrew O. Mellinger
 *
 * This is used by a bot to indicate that it has some work availability.
 */
public class MessageWorkerAvailability extends Message<EntityAIDrudge>
{
    public MessageWorkerAvailability(World world, EntityAIDrudge drudge)
    {
        super(drudge, drudge);
        _world = world;
    }

    public World getWorld()
    {
        return _world;
    }

    private final World _world;
}
