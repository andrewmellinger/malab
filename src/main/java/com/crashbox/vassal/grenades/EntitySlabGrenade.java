package com.crashbox.vassal.grenades;

import com.crashbox.vassal.util.VassalUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class EntitySlabGrenade extends EntityThrowable
{
    public EntitySlabGrenade(World world)
    {
        super(world);
    }

    public EntitySlabGrenade(World world, EntityPlayer playerEntity)
    {
        super(world, playerEntity);
    }

    @Override
    protected void onImpact(MovingObjectPosition mop)
    {
        if (!worldObj.isRemote)
        {
            if (mop.entityHit == null)
            {
                IBlockState stuff = Blocks.bedrock.getDefaultState();
                VassalUtils.flattenArea(worldObj, mop.getBlockPos(), 5, 4, stuff);
            }

            setDead();
        }
    }

}
