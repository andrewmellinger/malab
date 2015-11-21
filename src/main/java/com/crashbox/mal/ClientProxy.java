package com.crashbox.mal;

import com.crashbox.mal.chest.BlockBeaconChest;
import com.crashbox.mal.circuit.ItemCircuit;
import com.crashbox.mal.entity.BlockVassalHead;
import com.crashbox.mal.entity.EntityVassal;
import com.crashbox.mal.entity.RenderVassal;
import com.crashbox.mal.furnace.BlockBeaconFurnace;
import com.crashbox.mal.forester.BlockBeaconForester;
import com.crashbox.mal.quarry.BlockBeaconQuarry;
import com.crashbox.mal.workbench.BlockBeaconWorkbench;
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
        RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();

        // VASSAL
        RenderVassal renderer = new RenderVassal(renderManager, new ModelZombie(), 0.5F);
        RenderingRegistry.registerEntityRenderingHandler(EntityVassal.class, renderer);

        renderItem.getItemModelMesher().register(
                Item.getItemFromBlock(VassalMain.BLOCK_VASSAL_HEAD),
                0,
                new ModelResourceLocation(VassalMain.MODID + ":" + BlockBeaconWorkbench.NAME, "inventory"));

        // BLOCKS
        renderItem.getItemModelMesher().register(
                Item.getItemFromBlock(VassalMain.BLOCK_BEACON_CHEST),
                0,
                new ModelResourceLocation(VassalMain.MODID + ":" + BlockBeaconChest.NAME, "inventory"));

        renderItem.getItemModelMesher().register(
                Item.getItemFromBlock(VassalMain.BLOCK_BEACON_FURNACE),
                0,
                new ModelResourceLocation(VassalMain.MODID + ":" + BlockBeaconFurnace.NAME, "inventory"));

        renderItem.getItemModelMesher().register(
                Item.getItemFromBlock(VassalMain.BLOCK_BEACON_FORESTER),
                0,
                new ModelResourceLocation(VassalMain.MODID + ":" + BlockBeaconForester.NAME, "inventory"));

        renderItem.getItemModelMesher().register(
                Item.getItemFromBlock(VassalMain.BLOCK_BEACON_QUARRY),
                0,
                new ModelResourceLocation(VassalMain.MODID + ":" + BlockBeaconQuarry.NAME, "inventory"));

        renderItem.getItemModelMesher().register(
                Item.getItemFromBlock(VassalMain.BLOCK_BEACON_WORKBENCH),
                0,
                new ModelResourceLocation(VassalMain.MODID + ":" + BlockBeaconWorkbench.NAME, "inventory"));

        // Special block
        renderItem.getItemModelMesher().register(
                Item.getItemFromBlock(VassalMain.BLOCK_VASSAL_HEAD),
                0,
                new ModelResourceLocation(VassalMain.MODID + ":" + BlockVassalHead.NAME, "inventory"));

        //======================================
        // ITEMS

        renderItem.getItemModelMesher().register(VassalMain.ITEM_CIRCUIT, 0,
                new ModelResourceLocation(VassalMain.MODID + ":" + ItemCircuit.NAME, "inventory"));
    }

    @Override
    public void postInit(FMLPostInitializationEvent e)
    {

    }
}
