package com.crashbox.drudgemod.messaging;

import net.minecraft.util.BlockPos;

/**
 * Copyright 2015 Andrew O. Mellinger
 *
 *
 */
public interface IMessageSender
{
    /**
     * How far is the sender from the provided postion?  This is used for computing priority, etc.
     * We do this so that the sender can move, such as a mobile bot.
     * @param pos The post to compute to.
     * @return The distance to the position.
     */
    public int distanceTo(BlockPos pos);
}
