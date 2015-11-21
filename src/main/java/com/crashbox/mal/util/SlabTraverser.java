package com.crashbox.mal.util;

import com.crashbox.mal.util.MALUtils.COMPASS;
import net.minecraft.util.BlockPos;

import java.util.Iterator;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class SlabTraverser implements Iterable<BlockPos>
{
    public SlabTraverser(BlockPos center, BlockPos startingCorner, int radius, COMPASS dir)
    {
        _center = center;
        _starting = startingCorner;
        _radius = radius;
        _dir = dir;
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
            if (_dir == COMPASS.NORTH || _dir == COMPASS.SOUTH)
            {
                _minor = _starting.getX();
                _major = _starting.getZ();
                _minorDelta = _starting.getX() > _center.getX() ? -1 : 1;
                _majorDelta = _starting.getZ() > _center.getZ() ? -1 : 1;
                _minorCenter = _center.getX();
                _final = _center.getZ() + ((_radius + 1) * _majorDelta);
            }
            else
            {
                _minor = _starting.getZ();
                _major = _starting.getX();
                _minorDelta = _starting.getZ() > _center.getZ() ? -1 : 1;
                _majorDelta = _starting.getX() > _center.getX() ? -1 : 1;
                _minorCenter = _center.getZ();
                _final = _center.getX() + ((_radius + 1) * _majorDelta);
            }
        }

        @Override
        public boolean hasNext()
        {
            return _major != _final;
        }

        @Override
        public BlockPos next()
        {
            BlockPos result;
            if (_dir == COMPASS.NORTH || _dir == COMPASS.SOUTH)
                result = new BlockPos(_minor, _center.getY(), _major);
            else
                result = new BlockPos(_major, _center.getY(), _minor);

            _minor += _minorDelta;

            // If we are beyond end, switch direction and go back one,
            if (_minor > _minorCenter + _radius ||
                _minor < _minorCenter - _radius)
            {
                _minorDelta *= -1;
                _minor += _minorDelta;
                _major += _majorDelta;
            }

            return result;
        }

        @Override
        public void remove()
        {
            // Not supported
        }

        @Override
        public String toString()
        {
            return "SlabIterator{" +
                    "_minor=" + _minor +
                    ", _minorDelta=" + _minorDelta +
                    ", _major=" + _major +
                    ", _majorDelta=" + _majorDelta +
                    ", _final=" + _final +
                    '}';
        }

        private int _minor;
        private int _minorDelta;
        private int _minorCenter;
        private int _major;
        private final int _majorDelta;
        private final int _final;
    }

    @Override
    public String toString()
    {
        return "SlabTraverser{" +
                "_center=" + _center +
                ", _starting=" + _starting +
                ", _radius=" + _radius +
                ", _dir=" + _dir +
                '}';
    }

    private final BlockPos _center;
    private final BlockPos _starting;
    private final int _radius;
    private final MALUtils.COMPASS _dir;
}
