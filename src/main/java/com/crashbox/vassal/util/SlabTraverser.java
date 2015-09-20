package com.crashbox.vassal.util;

import net.minecraft.util.BlockPos;

import java.util.Iterator;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class SlabTraverser implements Iterable<BlockPos>
{
    public SlabTraverser(BlockPos center, int radius)
    {
        _center = center;
        _radius = radius;
    }

    @Override
    public Iterator<BlockPos> iterator()
    {
        return null;
    }

    private class SlabIterator implements Iterator<BlockPos>
    {
        SlabIterator()
        {
            _x = _center.getX() - _radius;
            _z = _center.getZ() - _radius;
        }

        @Override
        public boolean hasNext()
        {
            return _x != _center.getX() + _radius && _z != _center.getZ() + _radius;
        }

        @Override
        public BlockPos next()
        {
            BlockPos result = new BlockPos(_x, _center.getY(), _z);
            if (_x == _center.getX() + _radius)
            {
                _x = _center.getX() - _radius;
                _z += 1;
            }
            else
            {
                _x += 1;
            }

            return result;
        }

        @Override
        public void remove()
        {

        }

        private int _x;
        private int _z;
    }


    private final BlockPos _center;
    private final int _radius;
}
