package com.crashbox.vassal;

import com.crashbox.vassal.ai.EntityAIVassal;
import com.crashbox.vassal.chest.BlockBeaconChest;
import com.crashbox.vassal.entity.EntityVassal;
import com.crashbox.vassal.entity.RenderVassal;
import com.crashbox.vassal.furnace.BlockBeaconFurnace;
import com.crashbox.vassal.forester.BlockBeaconForester;
import com.crashbox.vassal.grenades.EntityDiggerGrenade;
import com.crashbox.vassal.grenades.EntityMineshaftGrenade;
import com.crashbox.vassal.grenades.ItemDiggerGrenade;
import com.crashbox.vassal.grenades.ItemMineshaftGrenade;
import com.crashbox.vassal.quarry.BlockBeaconQuarry;
import com.crashbox.vassal.workbench.BlockBeaconWorkbench;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelZombie;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderSnowball;
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
        RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();

        // VASSAL
        RenderVassal renderer = new RenderVassal(renderManager, new ModelZombie(), 0.5F);
        EntityAIVassal.setRenderVassal(renderer);
        RenderingRegistry.registerEntityRenderingHandler(EntityVassal.class, renderer);

        // BLOCKS
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

        renderItem.getItemModelMesher().register(
                Item.getItemFromBlock(VassalMain.BLOCK_BEACON_QUARRY),
                0,
                new ModelResourceLocation(VassalMain.MODID + ":" + BlockBeaconQuarry.NAME, "inventory"));

        // ITEMS
        renderItem.getItemModelMesher().register(VassalMain.ITEM_DIGGER_GRENADE, 0,
                new ModelResourceLocation(VassalMain.MODID + ":" + ItemDiggerGrenade.NAME, "inventory"));

        RenderingRegistry.registerEntityRenderingHandler(EntityDiggerGrenade.class,
                new RenderSnowball(renderManager, VassalMain.ITEM_DIGGER_GRENADE, renderItem));

        //---
        renderItem.getItemModelMesher().register(VassalMain.ITEM_MINESHAFT_GRENADE, 0,
                new ModelResourceLocation(VassalMain.MODID + ":" + ItemMineshaftGrenade.NAME, "inventory"));

        RenderingRegistry.registerEntityRenderingHandler(EntityMineshaftGrenade.class,
                new RenderSnowball(renderManager, VassalMain.ITEM_MINESHAFT_GRENADE, renderItem));
    }

    @Override
    public void postInit(FMLPostInitializationEvent e)
    {

    }
}
