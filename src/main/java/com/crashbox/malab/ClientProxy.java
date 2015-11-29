package com.crashbox.malab;

import com.crashbox.malab.chest.BlockAutoChest;
import com.crashbox.malab.circuit.ItemCircuit;
import com.crashbox.malab.workdroid.BlockWorkDroidHead;
import com.crashbox.malab.workdroid.EntityWorkDroid;
import com.crashbox.malab.workdroid.RenderWorkDroid;
import com.crashbox.malab.furnace.BlockAutoFurnace;
import com.crashbox.malab.forester.BlockAutoForester;
import com.crashbox.malab.quarry.BlockAutoQuarry;
import com.crashbox.malab.workbench.BlockAutoWorkbench;
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

        // DROID
        RenderWorkDroid renderer = new RenderWorkDroid(renderManager, new ModelZombie(), 0.5F);
        RenderingRegistry.registerEntityRenderingHandler(EntityWorkDroid.class, renderer);

        renderItem.getItemModelMesher().register(
                Item.getItemFromBlock(MALMain.BLOCK_DROID_HEAD),
                0,
                new ModelResourceLocation(MALMain.MODID + ":" + BlockAutoWorkbench.NAME, "inventory"));

        // BLOCKS
        renderItem.getItemModelMesher().register(
                Item.getItemFromBlock(MALMain.BLOCK_AUTO_CHEST),
                0,
                new ModelResourceLocation(MALMain.MODID + ":" + BlockAutoChest.NAME, "inventory"));

        renderItem.getItemModelMesher().register(
                Item.getItemFromBlock(MALMain.BLOCK_AUTO_FURNACE),
                0,
                new ModelResourceLocation(MALMain.MODID + ":" + BlockAutoFurnace.NAME, "inventory"));

        renderItem.getItemModelMesher().register(
                Item.getItemFromBlock(MALMain.BLOCK_AUTO_FORESTER),
                0,
                new ModelResourceLocation(MALMain.MODID + ":" + BlockAutoForester.NAME, "inventory"));

        renderItem.getItemModelMesher().register(
                Item.getItemFromBlock(MALMain.BLOCK_AUTO_QUARRY),
                0,
                new ModelResourceLocation(MALMain.MODID + ":" + BlockAutoQuarry.NAME, "inventory"));

        renderItem.getItemModelMesher().register(
                Item.getItemFromBlock(MALMain.BLOCK_AUTO_WORKBENCH),
                0,
                new ModelResourceLocation(MALMain.MODID + ":" + BlockAutoWorkbench.NAME, "inventory"));

        // Special block
        renderItem.getItemModelMesher().register(
                Item.getItemFromBlock(MALMain.BLOCK_DROID_HEAD),
                0,
                new ModelResourceLocation(MALMain.MODID + ":" + BlockWorkDroidHead.NAME, "inventory"));

        //======================================
        // ITEMS

        renderItem.getItemModelMesher().register(MALMain.ITEM_CIRCUIT, 0,
                new ModelResourceLocation(MALMain.MODID + ":" + ItemCircuit.NAME, "inventory"));
    }

    @Override
    public void postInit(FMLPostInitializationEvent e)
    {

    }
}
