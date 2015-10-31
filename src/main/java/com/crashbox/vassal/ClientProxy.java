package com.crashbox.vassal;

import com.crashbox.vassal.chest.BlockBeaconChest;
import com.crashbox.vassal.circuit.ItemCircuit;
import com.crashbox.vassal.entity.BlockVassalHead;
import com.crashbox.vassal.entity.EntityVassal;
import com.crashbox.vassal.entity.RenderVassal;
import com.crashbox.vassal.furnace.BlockBeaconFurnace;
import com.crashbox.vassal.forester.BlockBeaconForester;
import com.crashbox.vassal.quarry.BlockBeaconQuarry;
import com.crashbox.vassal.workbench.BlockBeaconWorkbench;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelZombie;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    private void registerSnowball(RenderManager renderManager, RenderItem renderItem,
                             Item item, String name, Class<? extends Entity> entityClass )
    {
        renderItem.getItemModelMesher().register(item, 0,
                new ModelResourceLocation(VassalMain.MODID + ":" + name, "inventory"));

        RenderingRegistry.registerEntityRenderingHandler(entityClass,
                new RenderSnowball(renderManager, item, renderItem));
    }

    @Override
    public void postInit(FMLPostInitializationEvent e)
    {

    }


    private static final Logger LOGGER = LogManager.getLogger();

}
