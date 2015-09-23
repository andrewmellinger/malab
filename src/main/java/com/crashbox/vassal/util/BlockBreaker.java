package com.crashbox.vassal.util;

import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

/**
 * Copyright CMU 2015.
 *
 * Updates world state about block breaking.
 */
public class BlockBreaker
{
    public BlockBreaker(World world, int entityID, BlockPos pos, int ticksNeeded)
    {
        _world = world;
        _entityID = entityID;
        _pos = pos;
        _ticksNeeded = ticksNeeded;
    }

    public boolean update()
    {
        // we have 10 stages
        ++_ticksDone;
        int i = (int)((float)_ticksDone/ _ticksNeeded * 10.0F);

        if (i != this._previousTicksDone)
        {
            _world.sendBlockBreakProgress(_entityID, _pos, i);
            this._previousTicksDone = i;
        }

        return (_ticksDone < _ticksNeeded);
    }

    public void reset()
    {
        _ticksDone = 0;
        _previousTicksDone = 0;
        _world.sendBlockBreakProgress(_entityID, _pos, 0);
    }

    private final World _world;
    private final int _entityID;

    private final BlockPos _pos;
    private final int _ticksNeeded;
    private int _ticksDone;
    private int _previousTicksDone;

}
