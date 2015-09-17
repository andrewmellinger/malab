package com.crashbox.vassal;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

/**
 * Copyright 2015 Andrew o. Mellinger
 */
public class RenderVassal extends RenderBiped
{

    private static final ResourceLocation vassalTexture = new ResourceLocation("vassal:textures/entity/vassal.png");

    public RenderVassal(RenderManager p_i46153_1_, ModelBiped p_i46153_2_, float p_i46153_3_)
    {
        super(p_i46153_1_, p_i46153_2_, p_i46153_3_);

        // TODO:  Should we remove the custom head renderer?
        LayerBipedArmor layerbipedarmor = new LayerBipedArmor(this);
        this.addLayer(layerbipedarmor);
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity)
    {
        return vassalTexture;
    }
}
