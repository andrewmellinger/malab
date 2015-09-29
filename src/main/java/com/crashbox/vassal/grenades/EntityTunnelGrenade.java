package com.crashbox.vassal.grenades;

import com.crashbox.vassal.VassalUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class EntityTunnelGrenade extends EntityThrowable
{
    public EntityTunnelGrenade(World world)
    {
        super(world);
    }

    public EntityTunnelGrenade(World world, EntityPlayer playerEntity)
    {
        super(world, playerEntity);
    }

    @Override
    protected void onImpact(MovingObjectPosition mop)
    {
        if (!worldObj.isRemote)
        {
            // Place a single torch if we didn't hit an entity
            if (mop.entityHit == null)
            {
                VassalUtils.digTunnel(worldObj, mop.getBlockPos(), 1, mop.sideHit.getOpposite(), 20, false, true);
            }

            setDead();
        }
    }


}
