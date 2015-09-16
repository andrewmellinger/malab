package com.crashbox.vassal;

import com.crashbox.vassal.chest.BlockBeaconChest;
import com.crashbox.vassal.furnace.BlockBeaconFurnace;
import com.crashbox.vassal.forester.BlockBeaconForester;
import com.crashbox.vassal.workbench.BlockBeaconWorkbench;
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
        RenderingRegistry.registerEntityRenderingHandler(EntityVassal.class,
                new RenderVassal(renderManager, new ModelZombie(), 0.5F));
//        RenderingRegistry.registerEntityRenderingHandler(EntityVassal.class,
//                new RenderVassal(renderManager));

        RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();

//        RenderingRegistry.registerEntityRenderingHandler(EntityThrowableTorch.class,
//                new RenderSnowball(renderManager, ThrowableTorchMod.ITEM_THROWABLE_TORCH, renderItem));

        // TODO: Explore this - what is meta
        renderItem.getItemModelMesher().register(
                Item.getItemFromBlock(VassalMain.BLOCK_BEACON_FURNACE),
                0,
                new ModelResourceLocation(VassalMain.MODID + ":" + BlockBeaconFurnace.NAME, "inventory"));

        renderItem.getItemModelMesher().register(
                Item.getItemFromBlock(VassalMain.BLOCK_BEACON_FORESTER),
                0,
                new ModelResourceLocation(VassalMain.MODID + ":" + BlockBeaconForester.NAME, "inventory"));

        renderItem.getItemModelMesher().register(
                Item.getItemFromBlock(VassalMain.BLOCK_BEACON_WORKBENCH),
                0,
                new ModelResourceLocation(VassalMain.MODID + ":" + BlockBeaconWorkbench.NAME, "inventory"));

        renderItem.getItemModelMesher().register(
                Item.getItemFromBlock(VassalMain.BLOCK_BEACON_CHEST),
                0,
                new ModelResourceLocation(VassalMain.MODID + ":" + BlockBeaconChest.NAME, "inventory"));
    }

    @Override
    public void postInit(FMLPostInitializationEvent e)
    {

    }
}
