package com.crashbox.mal.util;

import net.minecraft.util.BlockPos;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class BlockBounds
{
    public BlockBounds(BlockPos center, int radius)
    {
        _minX = center.getX() - radius;
        _minZ = center.getZ() - radius;
        _maxX = center.getX() + radius;
        _maxZ = center.getZ() + radius;
        _y = center.getY();
    }

    public BlockPos getMin()
    {
        return new BlockPos(_minX, _y, _minZ);
    }

    public BlockPos getMax()
    {
        return new BlockPos(_maxX, _y, _maxZ);
    }

    public boolean inBounds(BlockPos point)
    {
        return !( point.getX() < _minX || point.getX() > _maxX ||
                  point.getZ() < _minZ || point.getZ() > _maxZ );
    }

    @Override
    public String toString()
    {
        return "BlockBounds{" +
                "minX=" + _minX +
                ", minZ=" + _minZ +
                ", maxX=" + _maxX +
                ", maxZ=" + _maxZ +
                ", y=" + _y +
                '}';
    }

    private int _minX;
    private int _minZ;
    private int _maxX;
    private int _maxZ;
    private int _y;
}
