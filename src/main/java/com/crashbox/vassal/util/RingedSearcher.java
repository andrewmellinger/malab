package com.crashbox.vassal.util;

import com.crashbox.vassal.common.ItemStackMatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import java.util.*;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class RingedSearcher  implements Iterable<BlockPos>
{
    // Finds a tree looking in rings from the starting spot.
    public static Queue<BlockPos> findTree(World world, BlockPos start, int radius, int height,
                                           ItemStackMatcher matcher, List<BlockPos> exclusions,
                                           BlockBounds bounds)
    {
        RingedSearcher searcher = new RingedSearcher(start, radius, height);
        for (BlockPos pos : searcher)
        {
            if (VassalUtils.willDrop(world, pos, matcher) &&
                    bounds.inBounds(pos) &&
                    !VassalUtils.pointInAreas(pos, exclusions, 1))
            {
                Queue<BlockPos> result = new LinkedList<BlockPos>();

                // Move down to bottom
                for (int y = pos.getY(); y >= start.getY(); --y)
                {
                    BlockPos tmp = new BlockPos(pos.getX(), y, pos.getZ());
                    if (VassalUtils.willDrop(world, tmp, matcher))
                    {
                        result.add(tmp);
                    }
                }
                return result;
            }
        }

        return null;
    }


    public static Queue<BlockPos> findTree(World world, BlockPos center, int radius, int height,
            ItemStackMatcher matcher, List<BlockPos> exclusions)
    {
        RingedSearcher searcher = new RingedSearcher(center, radius, height);
        for (BlockPos pos : searcher)
        {
            if (VassalUtils.willDrop(world, pos, matcher) && !VassalUtils.pointInAreas(pos, exclusions, 1))
            {
                Queue<BlockPos> result = new LinkedList<BlockPos>();

                // Move down to bottom
                // TODO:  Also look laterally
                for (int y = pos.getY(); y >= center.getY(); --y)
                {
                    BlockPos tmp = new BlockPos(pos.getX(), y, pos.getZ());
                    if (VassalUtils.willDrop(world, tmp, matcher))
                    {
                        result.add(tmp);
                    }
                }
                return result;
            }
        }

        return null;
    }

    public static boolean detectBlock(World world, BlockPos center, int radius, int height, ItemStackMatcher matcher)
    {
        return (findBlock(world, center, radius, height, matcher) != null);
    }

    public static BlockPos findBlock(World world, BlockPos center, int radius, int height, ItemStackMatcher matcher)
    {
        RingedSearcher searcher = new RingedSearcher(center, radius, height);
        for (BlockPos pos : searcher)
        {
            if (VassalUtils.willDrop(world, pos, matcher))
            {
                return pos;
            }
        }

        return null;
    }

    public static ItemStack findFirstItemDrop(World world, BlockPos center, int radius, int height,
            ItemStackMatcher matcher)
    {
        RingedSearcher searcher = new RingedSearcher(center, radius, height);
        for (BlockPos pos : searcher)
        {
            ItemStack stack = VassalUtils.identifyWillDrop(world, pos, matcher);
            if (stack != null)
                return stack;
        }

        return null;
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
//        return new RingedIterator2(null);
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
                        if (_currentRadius > _radius)
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


    private enum DIR { EAST, SOUTH, WEST, NORTH }

    /**
     * This searcher looks in rings from the center out, then looks at the next layer.
     * So like a tree, concentric circles, then down.
     */
    public class RingedIterator2 implements Iterator<BlockPos>
    {
        public RingedIterator2(List<BlockPos> exclusions)
        {
            _currentRadius = 1;
            _x = _center.getX() - _currentRadius;
            _y = _center.getY() + _height;
            _z = _center.getZ() - _currentRadius;
            _exclusions = exclusions;
        }

        @Override
        public boolean hasNext()
        {
            return (_x != _center.getX() - _radius ||
                    _z != _center.getZ() - _radius ||
                    _y != _center.getY() - 1);
        }

        @Override
        public BlockPos next()
        {
            BlockPos result = new BlockPos(_x, _y, _z);

            // We need to repeat until we get a good block
            nextBlock();

            while (!isValid() && hasNext())
                nextBlock();

            return result;
        }

        private void nextBlock()
        {
            switch (_dir)
            {
                case EAST:
                    if (_x == _center.getX() + _currentRadius)
                    {
                        _z += 1;
                        _dir = DIR.SOUTH;
                    }
                    else
                    {
                        _x += 1;
                    }
                    break;
                case SOUTH:
                    if (_z == _center.getZ() + _currentRadius)
                    {
                        _x -= 1;
                        _dir = DIR.WEST;
                    }
                    else
                    {
                        _z += 1;
                    }
                    break;
                case WEST:
                    if (_x == _center.getX() - _currentRadius)
                    {
                        _z -= 1;
                        _dir = DIR.NORTH;
                    }
                    else
                    {
                        _x -= 1;
                    }
                    break;
                case NORTH:
                    if (_z == _center.getZ() - _currentRadius)
                    {
                        _x += 1;
                        _dir = DIR.EAST;
                    }
                    else
                    {
                        _z -= 1;
                    }
                    break;
            }

            // If we are at start corner do next ring, or move up.
            if (_x == _center.getX() - _currentRadius &&
                _z == _center.getZ() - _currentRadius)
            {
                if (_currentRadius == _radius)
                {
                    _currentRadius = 1;
                    _y -= 1;
                    _x = _center.getX() - _currentRadius;
                    _z = _center.getZ() - _currentRadius;
                }
                else
                {
                    _currentRadius += 1;
                    System.out.println("Current radius: " + _currentRadius);
                    _x = _center.getX() - _currentRadius;
                    _z = _center.getZ() - _currentRadius;
                }
            }
        }

        private boolean isValid()
        {
            if (_exclusions == null)
                return true;

            return !VassalUtils.pointInAreas(new BlockPos(_x, _y, _z), _exclusions, 1);
        }


        @Override
        public void remove()
        {
            // We do nothing
        }

        private int _x;
        private int _y;
        private int _z;
        private DIR _dir = DIR.EAST;
        private int _currentRadius;
        private List<BlockPos> _exclusions;
    }


    private final BlockPos _center;
    private final int _radius;
    private final int _height;


}

