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
        return new SlabIterator();
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
            return  _z != _center.getZ() + _radius + 1;
        }

        @Override
        public BlockPos next()
        {
            BlockPos result = new BlockPos(_x, _center.getY(), _z);
            _x += _xDelta;

            // If we are beyond end, switch direction and go back one,
            if (_x > _center.getX() + _radius ||
                _x < _center.getX() - _radius)
            {
                _xDelta *= -1;
                _x += _xDelta;
                _z += 1;
            }

            return result;
        }

        @Override
        public void remove()
        {
            // Not supported
        }

        private int _x;
        private int _xDelta = 1;
        private int _z;

    }


    private final BlockPos _center;
    private final int _radius;
}
