package com.crashbox.drudgemod;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

/**
 * Copyright 2015 Andrew o. Mellinger
 */
public class RenderDrudge extends RenderBiped
{

//    private static final ResourceLocation zombieTextures = new ResourceLocation("textures/entity/zombie/zombie.png");
    private static final ResourceLocation zombieTextures = new ResourceLocation("drudgemod:textures/entity/drudge.png");

//    public RenderDrudge(RenderManager p_i46127_1_)
//    {
//        super(p_i46127_1_);
//    }

    public RenderDrudge(RenderManager p_i46153_1_, ModelBiped p_i46153_2_, float p_i46153_3_)
    {
        super(p_i46153_1_, p_i46153_2_, p_i46153_3_);
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity)
    {
        return zombieTextures;
    }
}
