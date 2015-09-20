package com.crashbox.vassal.util;

import net.minecraft.util.BlockPos;
import com.crashbox.vassal.VassalUtils.COMPASS;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class BlockWalker
{
    public BlockWalker(BlockPos current, boolean down)
    {
        _current = new MutablePos(current.getX(), current.getY(), current.getZ());
        _down = down;
    }

    public BlockWalker(BlockPos current, boolean down, COMPASS dir)
    {
        _current = new MutablePos(current.getX(), current.getY(), current.getZ());
        _down = down;
        _direction = dir.ordinal();
    }

    public BlockPos getPos()
    {
        return _current.makeBlockPos();
    }

    public void forward()
    {
        CLOCKWISE[_direction].forward(_current);
    }

    public void turnLeft()
    {
        _direction -= 1;
        if (_direction == -1)
            _direction = 3;
    }

    public void turnRight()
    {
        _direction += 1;
        if (_direction == 4)
            _direction = 0;
    }

    public int down()
    {
        _current._y -= 1;
        return _current._y;
    }

    public int downHalf()
    {
        if (_down)
            _current._y -= 1;
        _down = !_down;
        return _current._y;
    }

    public boolean isDown()
    {
        return _down;
    }

    /**
     * Gets a slab of blocks based on our current direction, extending the specified
     * number to the left and right.  They will all be at the same height
     * @param left Number of blocks to the left.
     * @param right Number of blocks to the right.
     * @return List of blocks.
     */
    public BlockPos[] getRow(int left, int right)
    {
        BlockPos[] result = new BlockPos[left + 1 + right];

        int idx = 0;
        for (int i = left; i > 0; --i)
        {
            result[i] = CLOCKWISE[_direction].left(_current, i);
            ++idx;
        }

        result[idx] = _current.makeBlockPos();
        ++idx;

        for (int i = 1; i <= right; ++i)
        {
            result[i] = CLOCKWISE[_direction].right(_current, i);
            ++idx;
        }

        return result;
    }


    public static Incrementer[] CLOCKWISE = { new IncrementerEast(),
            new IncrementerSouth(),
            new IncrementerWest(),
            new IncrementerNorth() };

    public static abstract class Incrementer
    {
        public abstract void forward(MutablePos pos);
        public abstract BlockPos left(MutablePos pos, int offset);
        public abstract BlockPos right(MutablePos pos, int offset);
    }

    public static class IncrementerEast extends Incrementer
    {
        @Override
        public void forward(MutablePos pos)                { pos._x += 1; }

        @Override
        public BlockPos left(MutablePos pos, int offset)   { return pos.makeOffset( 0, 0, offset * -1 ); }

        @Override
        public BlockPos right(MutablePos pos, int offset)  { return pos.makeOffset( 0, 0, offset ); }
    }

    public static class IncrementerSouth extends Incrementer
    {
        @Override
        public void forward(MutablePos pos)                { pos._z += 1; }

        @Override
        public BlockPos left(MutablePos pos, int offset)   { return pos.makeOffset( offset, 0, 0 ); }

        @Override
        public BlockPos right(MutablePos pos, int offset)  { return pos.makeOffset( offset * -1, 0, 0 ); }
    }

    public static class IncrementerWest extends Incrementer
    {
        @Override
        public void forward(MutablePos pos)                { pos._x -= 1; }

        @Override
        public BlockPos left(MutablePos pos, int offset)   { return pos.makeOffset( 0, 0, offset ); }

        @Override
        public BlockPos right(MutablePos pos, int offset)  { return pos.makeOffset( 0, 0, offset * -1 ); }
    }

    public static class IncrementerNorth extends Incrementer
    {
        @Override
        public void forward(MutablePos pos)                { pos._z -= 1; }

        @Override
        public BlockPos left(MutablePos pos, int offset)   { return pos.makeOffset( offset * -1, 0, 0 ); }

        @Override
        public BlockPos right(MutablePos pos, int offset)  { return pos.makeOffset( offset, 0, 0 ); }
    }





    private int _direction = 0;
    private MutablePos _current;
    private boolean _down;
}
