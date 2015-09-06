package com.crashbox.drudgemod.messaging;

import net.minecraft.util.BlockPos;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class MessageGoTo extends Message
{
    public MessageGoTo(IMessager sender, BlockPos pos)
    {
        super(sender, null, null, 0);
        _pos = pos;
    }

    public BlockPos getPos()
    {
        return _pos;
    }

    private final BlockPos _pos;
}
