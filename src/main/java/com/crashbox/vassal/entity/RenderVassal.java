package com.crashbox.vassal.entity;

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
    private static final ResourceLocation specialTexture = new ResourceLocation("vassal:textures/entity/vassal_working.png");

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
        return _resourceLocation;
    }

    public static enum VASSAL_TEXTURE { NORMAL, WORKING}

    public void setTexture(VASSAL_TEXTURE texture)
    {
        switch (texture)
        {
            case NORMAL:
                _resourceLocation = vassalTexture;
                break;
            case WORKING:
                _resourceLocation = specialTexture;
                break;
        }
    }

    private ResourceLocation _resourceLocation = vassalTexture;

}
