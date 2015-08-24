package com.crashbox.drudgemod.ai;

import com.crashbox.drudgemod.DrudgeUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import java.util.*;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class RingedSearcher  implements Iterable<BlockPos>
{
    // UNDERSTANDS LOG METADATA
    public static Queue<BlockPos> findTree(World world, BlockPos center, int radius, int height, ItemStack sample)
    {
        RingedSearcher searcher = new RingedSearcher(center, radius, height);
        for (BlockPos pos : searcher)
        {
            if (DrudgeUtils.willDrop(world, pos, sample))
            {
                Queue<BlockPos> result = new LinkedList<BlockPos>();

                // Move down to bottom
                // TODO:  Also look laterally
                for (int y = pos.getY(); y >= center.getY(); --y)
                {
                    BlockPos tmp = new BlockPos(pos.getX(), y, pos.getZ());
                    if (DrudgeUtils.willDrop(world, tmp, sample))
                    {
                        result.add(tmp);
                    }
                }
                return result;
            }
        }

        return null;
    }

    public static boolean findBlock(World world, BlockPos center, int radius, int height, ItemStack sample)
    {
        RingedSearcher searcher = new RingedSearcher(center, radius, height);
        for (BlockPos pos : searcher)
        {
            if (DrudgeUtils.willDrop(world, pos, sample))
            {
                return true;
            }
        }

        return false;
    }


    public RingedSearcher(BlockPos center, int radius, int height)
    {
        _center = center;
        _radius = radius;
        _height = height;
    }

    @Override
    public Iterator<BlockPos> iterator()
    {
        return new RingedIterator();
    }

    public class RingedIterator implements Iterator<BlockPos>
    {
        public RingedIterator()
        {
            _currentRadius = 0;
            _done = false;
            initRing();
        }

        @Override
        public boolean hasNext()
        {
            return !_done;
        }

        @Override
        public BlockPos next()
        {
            if (_done)
            {
                return null;
            }

            // Construct the current one
            BlockPos retVal = new BlockPos(_x, _y, _z);

            if (_x == _maxX)
            {
                if (_z < _maxZ)
                {
                    // Next row
                    _x = _minX;
                    _z += 1;
                }
                else
                {
                    if (_y > _minY)
                    {
                        // next layer
                        _x = _minX;
                        _z = _minZ;
                        _y -= 1;
                    }
                    else
                    {
                        // next ring
                        _currentRadius += 1;
                        if (_currentRadius == _radius)
                        {
                            _done = true;
                        }
                        initRing();
                    }
                }
            }
            else
            {
                // Walk the row, or go to end of row
                if ( _z == _minZ || _z == _maxZ)
                    _x += 1;
                else
                    _x = _maxX;
            }

            return retVal;
        }

        @Override
        public void remove()
        {
            // We do nothing
        }

        private void initRing()
        {
            _x = _minX = _center.getX() - _currentRadius;
            _z = _minZ = _center.getZ() - _currentRadius;
            _maxX = _center.getX() + _currentRadius;
            _maxZ = _center.getZ() + _currentRadius;
            _y = _center.getY() + _height;
            _minY = _center.getY();

            // Don't check the center block
            if (_currentRadius == 0)
                _minY += 1;
        }

        private boolean _done = false;
        private int _x, _minX, _maxX;
        private int _y, _minY;
        private int _z, _minZ, _maxZ;
        private int _currentRadius = 0;
    }

    private final BlockPos _center;
    private final int _radius;
    private final int _height;


}

