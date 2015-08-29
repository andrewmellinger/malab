package com.crashbox.drudgemod.messaging;

import com.crashbox.drudgemod.ai.EntityAIDrudge;
import com.crashbox.drudgemod.messaging.Message;
import net.minecraft.world.World;

/**
 * Copyright 2015 Andrew O. Mellinger
 *
 * This is used by a bot to indicate that it has some work availability.
 */
public class MessageWorkerAvailability extends Message
{
    /**
     * Send a nmessage saying this worker is available
     * @param world The world object we are i
     * @param drudge The AI that will perform the work.
     */
    public MessageWorkerAvailability(World world, EntityAIDrudge drudge)
    {
        super(drudge, null);
        _drudgeAI = drudge;
        _world = world;
    }

    public World getWorld()
    {
        return _world;
    }

    public EntityAIDrudge getAIDrudge()
    {
        return _drudgeAI;
    }

    private final EntityAIDrudge _drudgeAI;
    private final World _world;
}
