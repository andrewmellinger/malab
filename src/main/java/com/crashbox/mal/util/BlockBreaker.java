package com.crashbox.mal.util;

import com.crashbox.mal.workdroid.EntityWorkDroid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

/**
 * Copyright CMU 2015.
 *
 * Updates world state about block breaking.
 */
public class BlockBreaker
{
    public static float breakSeconds(World world, EntityWorkDroid entity, BlockPos pos)
    {
        // A stone pickaxe takes about 1 second to break. So I can break 10 in 9 seconds
        // Stone hardness = 1.5
        // Stone pickaxe has a 4x dig speed
        // A 1.0 workspeed is steve.  The bots by default are slower.
        //
        // 1.0 = ( x * 1.5) / ( 4 * 1.0 )
        // 4 = x * 1.5
        // 4/1.5 = x ~ 2.5

        // If we don't find a tool stack, we don't use one.
        ItemStack toolStack = entity.findBestTool(pos);
        IBlockState state = world.getBlockState(pos);

        float digSpeed = 1.0F;
        if (toolStack != null)
            digSpeed = toolStack.getItem().getDigSpeed(toolStack, state);

        float getHardness = state.getBlock().getBlockHardness(world, pos);

        return (BASE_BREAK_TIME * getHardness) / (digSpeed * entity.getWorkSpeedFactor()) ;
    }

    public BlockBreaker(World world, EntityWorkDroid entity, BlockPos pos)
    {
        _world = world;
        _entityID = entity.getEntityId();
        _pos = pos;
        _ticksNeeded = (int)(breakSeconds(world, entity, pos) * 20);
    }

    public boolean isStillBreaking()
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

    private static final float BASE_BREAK_TIME = 2.5F;
}
