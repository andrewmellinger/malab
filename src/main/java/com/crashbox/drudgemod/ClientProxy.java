package com.crashbox.drudgemod;

import com.crashbox.drudgemod.furnace.BlockTaskerFurnace;
import com.crashbox.drudgemod.lumberjack.BlockTaskerLumberjack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelZombie;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class ClientProxy extends CommonProxy
{
    @Override
    public void preInit(FMLPreInitializationEvent e)
    {

    }

    @Override
    public void init(FMLInitializationEvent e)
    {
        // Add renderers.
        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
        System.out.println("renderManager: " + renderManager);
        RenderingRegistry.registerEntityRenderingHandler(EntityDrudge.class,
                new RenderDrudge(renderManager, new ModelZombie(), 0.5F));
//        RenderingRegistry.registerEntityRenderingHandler(EntityDrudge.class,
//                new RenderDrudge(renderManager));

        RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();

//        RenderingRegistry.registerEntityRenderingHandler(EntityThrowableTorch.class,
//                new RenderSnowball(renderManager, ThrowableTorchMod.ITEM_THROWABLE_TORCH, renderItem));

        // TODO: Explore this - what is meta
        renderItem.getItemModelMesher().register(
                Item.getItemFromBlock(DrudgeMain.BLOCK_TASKER_FURNACE),
                0,
                new ModelResourceLocation(DrudgeMain.MODID + ":" + BlockTaskerFurnace.NAME, "inventory"));
        renderItem.getItemModelMesher().register(
                Item.getItemFromBlock(DrudgeMain.BLOCK_TASKER_LUMBERJACK),
                0,
                new ModelResourceLocation(DrudgeMain.MODID + ":" + BlockTaskerLumberjack.NAME, "inventory"));
    }

    @Override
    public void postInit(FMLPostInitializationEvent e)
    {

    }
}
