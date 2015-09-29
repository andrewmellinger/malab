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
public class EntityFlattenGrenade extends EntityThrowable
{
    public EntityFlattenGrenade(World world)
    {
        super(world);
    }

    public EntityFlattenGrenade(World world, EntityPlayer playerEntity)
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
                IBlockState dirt = Blocks.dirt.getDefaultState();
                VassalUtils.flattenArea(worldObj, mop.getBlockPos(), 5, 4, dirt);
            }

            setDead();
        }
    }


}
