package com.crashbox.vassal.grenades;

import com.crashbox.vassal.util.VassalUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class EntityDiggerGrenade extends EntityThrowable
{
    public EntityDiggerGrenade(World world)
    {
        super(world);
    }

    public EntityDiggerGrenade(World world, EntityPlayer playerEntity)
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
                VassalUtils.digColumn(worldObj, mop.getBlockPos(), 1, 10, false);
            }

            setDead();
        }
    }
}
