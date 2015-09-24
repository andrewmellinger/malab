package com.crashbox.vassal.util;

import net.minecraft.util.BlockPos;

import java.util.Iterator;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class SlabTraverser implements Iterable<BlockPos>
{
    public SlabTraverser(BlockPos center, BlockPos startingCorner, int radius)
    {
        _center = center;
        _starting = startingCorner;
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
            _x = _starting.getX();
            _z = _starting.getZ();
            _xDelta = _starting.getX() > _center.getX() ? -1 : 1;
            _zDelta = _starting.getZ() > _center.getZ() ? -1 : 1;
            _zFinal = _center.getZ() + (( _radius + 1 ) * _zDelta);
        }

        @Override
        public boolean hasNext()
        {
            return  _z != _zFinal;
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
                _z += _zDelta;
            }

            return result;
        }

        @Override
        public void remove()
        {
            // Not supported
        }

        private int _x;
        private int _xDelta;
        private int _z;
        private final int _zDelta;
        private final int _zFinal;

    }

    private final BlockPos _center;
    private final BlockPos _starting;
    private final int _radius;
}
