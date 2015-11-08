package com.crashbox.vassal.ai;

import com.crashbox.vassal.entity.EntityVassal;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateGround;

/**
 * Copyright 2015 Andrew O. Mellinger
 * <p>
 * This AI module is used to make the entity follow a specific player.  It is designed
 * to work with the vassal bot, which exposes the "getFollowPlayer" function.
 * The bot will try to keep up, but will stop moving if gets too close as to prevent
 * "crowding."
 */
public class EntityAIFollowPlayer extends EntityAIBase
{
    private final EntityVassal _vassal;
    private EntityPlayer _player;
    private PathNavigate _botPathfinder;
    private final double _maxDistSq;
    private final double _minDistSq;
    private int _tickCounter;

    public EntityAIFollowPlayer(EntityVassal vassal, float minDist, float maxDist)
    {
        _vassal = vassal;
        _botPathfinder = vassal.getNavigator();
        _minDistSq = minDist * minDist;
        _maxDistSq = maxDist * maxDist;
        setMutexBits(3);

        if (!(vassal.getNavigator() instanceof PathNavigateGround))
        {
            throw new IllegalArgumentException("Unsupported mob type for EntityAIFollowPlayer");
        }
    }

    /** We follow if we are set to follow and
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        _player = _vassal.getFollowPlayer();
        return (_player != null);
    }

    /** Keep moving while it is the same player, and the we aren't close enough
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean continueExecuting()
    {
        return (_vassal.getFollowPlayer() == _player);
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
        _tickCounter = 0;
        ((PathNavigateGround)_vassal.getNavigator()).func_179690_a(false);
    }

    /**
     * Resets the task
     */
    public void resetTask()
    {
        _player = null;
        _botPathfinder.clearPathEntity();
        ((PathNavigateGround)_vassal.getNavigator()).func_179690_a(true);
    }

    /**
     * Updates the task
     */
    public void updateTask()
    {
        _vassal.getLookHelper().setLookPositionWithEntity(_player, 10.0F, (float) _vassal.getVerticalFaceSpeed());

        if (_vassal.getFollowPlayer() != _player)
            return;

        // If we are out of range, just hold.
        double distanceSq = _vassal.getDistanceSqToEntity(_player);
        if (distanceSq < _minDistSq || distanceSq > _maxDistSq)
        {
            // If we are out of range, just hang out
            _botPathfinder.clearPathEntity();
            return;
        }

        // Only recompute every second
        _vassal.burnFuel();
        if (--_tickCounter <= 0)
        {
            _tickCounter = 20;

            // Just keep trying to get there
            _botPathfinder.tryMoveToEntityLiving(_player, _vassal.getSpeedFactor());
            //_vassal.sendParticleMessage(EnumParticleTypes.HEART, 22);

//            if (!_botPathfinder.tryMoveToEntityLiving(_player, _speed))
//            {
//                if (!_vassal.getLeashed())
//                {
//                    if (_vassal.getDistanceSqToEntity(_player) >= 144.0D)
//                    {
//                        int i = MathHelper.floor_double(_player.posX) - 2;
//                        int j = MathHelper.floor_double(_player.posZ) - 2;
//                        int k = MathHelper.floor_double(_player.getEntityBoundingBox().minY);
//
//                        for (int l = 0; l <= 4; ++l)
//                        {
//                            for (int i1 = 0; i1 <= 4; ++i1)
//                            {
//                                if ((l < 1 || i1 < 1 || l > 3 || i1 > 3) &&
//                                        World.doesBlockHaveSolidTopSurface(_world, new BlockPos(i + l, k - 1, j + i1)) &&
//                                        !_world.getBlockState(new BlockPos(i + l, k, j + i1)).getBlock().isFullCube() &&
//                                        !_world.getBlockState(new BlockPos(i + l, k + 1, j + i1)).getBlock().isFullCube())
//                                {
//                                    _vassal.setLocationAndAngles((double)((float)(i + l) + 0.5F), (double)k, (double)((float)(j + i1) + 0.5F), _vassal.rotationYaw, _vassal.rotationPitch);
//                                    _botPathfinder.clearPathEntity();
//                                    return;
//                                }
//                            }
//                        }
//                    }
//                }
//            }
        }
    }
}
