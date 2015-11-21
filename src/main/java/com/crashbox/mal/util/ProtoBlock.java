package com.crashbox.mal.util;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;

/**
 * Copyright Andrew O. Mellinger.
 *
 * This represents a proposed block.  So a block position and desired
 * block state.
 */
public class ProtoBlock
{
    public ProtoBlock(BlockPos pos, IBlockState state)
    {
        _pos = pos;
        _state = state;
    }

    public BlockPos getPos()
    {
        return _pos;
    }

    public IBlockState getState()
    {
        return _state;
    }

    private final BlockPos _pos;
    private final IBlockState _state;
}
