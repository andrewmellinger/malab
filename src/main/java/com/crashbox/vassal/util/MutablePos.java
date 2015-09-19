package com.crashbox.vassal.util;

import net.minecraft.util.BlockPos;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class MutablePos
{
    public int _x;
    public int _y;
    public int _z;

    public MutablePos(int x, int y, int z)
    {
        _x = x;
        _y = y;
        _z = z;
    }

    public MutablePos(BlockPos pos)
    {
        _x = pos.getX();
        _y = pos.getY();
        _z = pos.getY();
    }

    public BlockPos makeBlockPos()
    {
        return new BlockPos(_x, _y, _z);
    }

    public BlockPos makeOffset(int dX, int dY, int dZ )
    {
        return new BlockPos(_x + dX, _y + dY, _z + dZ);
    }
}
