package com.crashbox.vassal.grenades;

import com.crashbox.vassal.util.VassalUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class EntityWallGrenade extends EntityThrowable
{
    public EntityWallGrenade(World world)
    {
        super(world);
    }

    public EntityWallGrenade(World world, EntityPlayer playerEntity)
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
                BlockPos min, max;
                LOGGER.debug("hitVec=" + mop.hitVec);

                EntityLivingBase thrower = getThrower();
                double deltaX = Math.abs(thrower.getPosition().getX() - mop.getBlockPos().getX());
                double deltaZ = Math.abs(thrower.getPosition().getZ() - mop.getBlockPos().getZ());

                if (deltaX > deltaZ)
                {
                    min = new BlockPos(mop.getBlockPos().getX(),
                            mop.getBlockPos().getY(),
                            mop.getBlockPos().getZ() - 4);
                    max = new BlockPos(mop.getBlockPos().getX(),
                            mop.getBlockPos().getY() + 8,
                            mop.getBlockPos().getZ() + 4);
                }
                else
                {
                    min = new BlockPos(mop.getBlockPos().getX() - 4,
                            mop.getBlockPos().getY(),
                            mop.getBlockPos().getZ());
                    max = new BlockPos(mop.getBlockPos().getX() + 4,
                            mop.getBlockPos().getY() + 8,
                            mop.getBlockPos().getZ());
                }
                VassalUtils.fillArea(worldObj, min, max, stuff);
            }

            setDead();
        }
    }

    private static final Logger LOGGER = LogManager.getLogger();

}
