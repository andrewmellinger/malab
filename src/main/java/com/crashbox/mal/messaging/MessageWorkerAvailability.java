package com.crashbox.mal.messaging;

import com.crashbox.mal.ai.EntityAIWorkDroid;
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
     * @param workDroid The AI that will perform the work.
     */
    public MessageWorkerAvailability(World world, EntityAIWorkDroid workDroid)
    {
        super(workDroid, null, MessageWorkerAvailability.class, 0);
        _workDroidAI = workDroid;
        _world = world;
    }

    public World getWorld()
    {
        return _world;
    }

    public EntityAIWorkDroid getAIWorkDroid()
    {
        return _workDroidAI;
    }

    private final EntityAIWorkDroid _workDroidAI;
    private final World _world;
}
