package com.crashbox.drudgemod.ai;

import com.crashbox.drudgemod.messaging.Message;
import com.crashbox.drudgemod.messaging.IMessageSender;
import net.minecraft.util.BlockPos;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class MessageGoTo extends Message
{
    public MessageGoTo(IMessageSender sender, BlockPos pos)
    {
        super(sender);
        _pos = pos;
    }

    public BlockPos getPos()
    {
        return _pos;
    }

    private final BlockPos _pos;
}
