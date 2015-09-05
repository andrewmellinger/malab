package com.crashbox.drudgemod.messaging;

import net.minecraft.util.BlockPos;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class MessageWorkArea extends Message
{
    public MessageWorkArea(IMessager sender, IMessager target, Object cause, BlockPos area)
    {
        super(sender, target, cause);
        _pos = new BlockPos(area);
    }

    public BlockPos getWorkArea()
    {
        return _pos;
    }

    private final BlockPos _pos;
}
