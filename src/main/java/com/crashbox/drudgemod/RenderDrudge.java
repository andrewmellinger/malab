package com.crashbox.drudgemod;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderEntity;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderZombie;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

/**
 * Copyright 2015 Andrew o. Mellinger
 */
//public class RenderDrudge extends RenderZombie
public class RenderDrudge extends RenderLiving
{

    private static final ResourceLocation zombieTextures = new ResourceLocation("textures/entity/zombie/zombie.png");
//    private static final ResourceLocation zombieTextures = new ResourceLocation("mob/zombie.png");

//    public RenderDrudge(RenderManager p_i46127_1_)
//    {
//        super(p_i46127_1_);
//    }

    public RenderDrudge(RenderManager p_i46153_1_, ModelBase p_i46153_2_, float p_i46153_3_)
    {
        super(p_i46153_1_, p_i46153_2_, p_i46153_3_);
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity)
    {
        return zombieTextures;
    }


}
