package com.crashbox.mal.util;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class OrchardTraverser implements Iterable<BlockPos>
{
    public OrchardTraverser(World world, BlockPos center, int radius, int height, int depth, List<Block> allowable)
    {
        _world = world;
        _center = center;
        _radius = radius - (radius % 2);
        _height = height;
        _depth = depth;
        _blockList = allowable;
    }

    @Override
    public Iterator<BlockPos> iterator()
    {
        return new OrchardIterator();
    }

    private class OrchardIterator implements Iterator<BlockPos>
    {
        private OrchardIterator()
        {
            _x = _center.getX() - _radius;
            _z = _center.getZ() - _radius;
            findNext();
        }

        @Override
        public boolean hasNext()
        {
            return _next != null;
        }

        @Override
        public BlockPos next()
        {
            BlockPos result = _next;
            findNext();
            return result;
        }

        @Override
        public void remove()
        {
            // Unsupported
        }

        //
        private void findNext()
        {
            // Move through the next positions looking for a block.
            _next = null;
            while (true)
            {
                // Do we do even or odd
                if (_count % 2 == _evenOdd)
                    _next = findSpotAboveGround(new BlockPos(_x, _center.getY(), _z));

                // Found one we are good!
                if (_next != null)
                    return;

                // Increment count
                ++_count;
                _x += 2;

                if (_x > _center.getX() + _radius)
                {
                    _z += 2;
                    _x = _center.getX() - _radius;
                }

                if (_z > _center.getZ() + _radius)
                {
                    // Once we run out of Z on the even pass we are done
                    if (_evenOdd == 1)
                        return;

                    _evenOdd = 1;
                    _count = 0;
                    _x = _center.getX() - _radius;
                    _z = _center.getZ() - _radius;
                }
            }
        }

        private BlockPos findSpotAboveGround(BlockPos start)
        {
            List<Block> downList = makeDownList();
            List<Block> upList = makeUpList();

            // Start at the top and work down.  Pos is the "up" block
            BlockPos pos = start.up(_height);
            IBlockState upState = _world.getBlockState(pos);
            pos = pos.down();
            while (pos.getY() > start.getY() - _depth)
            {
                IBlockState downState = _world.getBlockState(pos.down());

                if (downList.contains(downState.getBlock()) && upList.contains(upState.getBlock()))
                    return pos;

                upState = downState;
                pos = pos.down();
            }
            return null;
        }

        private List<Block> makeDownList()
        {
            List<Block> downList = new ArrayList<Block>();
            downList.add(Blocks.grass);
            downList.add(Blocks.dirt);
            return downList;
        }

        private List<Block> makeUpList()
        {
            if (_blockList != null)
                return _blockList;

            List<Block> upList = new ArrayList<Block>();
            upList.add(Blocks.air);
            return upList;
        }


        // This is the area we scan
        private int _x;
        private int _z;
        private int _count = 0;
        private int _evenOdd = 0;

        private BlockPos _next;
    }

    private final World _world;
    private final BlockPos _center;
    private final int _radius;
    private final int _height;
    private final int _depth;
    private final List<Block> _blockList;
}

