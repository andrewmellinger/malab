package com.crashbox.mal.messaging;

import com.crashbox.mal.ai.EntityAIVassal;
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
     * @param vassal The AI that will perform the work.
     */
    public MessageWorkerAvailability(World world, EntityAIVassal vassal)
    {
        super(vassal, null, MessageWorkerAvailability.class, 0);
        _vassalAI = vassal;
        _world = world;
    }

    public World getWorld()
    {
        return _world;
    }

    public EntityAIVassal getAIVassal()
    {
        return _vassalAI;
    }

    private final EntityAIVassal _vassalAI;
    private final World _world;
}
