package com.crashbox.mal;

import com.crashbox.mal.ai.Priority;
import com.crashbox.mal.chest.BlockAutoChest;
import com.crashbox.mal.chest.TileEntityAutoChest;
import com.crashbox.mal.circuit.ItemCircuit;
import com.crashbox.mal.util.MALEventHandler;
import com.crashbox.mal.workdroid.BlockWorkDroidHead;
import com.crashbox.mal.workdroid.EntityWorkDroid;
import com.crashbox.mal.furnace.BlockAutoFurnace;
import com.crashbox.mal.furnace.TileEntityAutoFurnace;
import com.crashbox.mal.forester.BlockAutoForester;
import com.crashbox.mal.forester.TileEntityAutoForester;
import com.crashbox.mal.network.MessageWorkDroidEffects;
import com.crashbox.mal.quarry.BlockAutoQuarry;
import com.crashbox.mal.quarry.TileEntityAutoQuarry;
import com.crashbox.mal.workbench.BlockAutoWorkbench;
import com.crashbox.mal.workbench.TileEntityAutoWorkbench;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
@Mod(modid= MALMain.MODID, name= MALMain.NAME, version= MALMain.VERSION)
public class MALMain
{
    // This guy talks about what each event handler does
    // http://greyminecraftcoder.blogspot.com/2013/11/how-forge-starts-up-your-code.html
    public static final String MODID = "mal";
    public static final String NAME = "MAL";
    public static final String VERSION = "0.9.004";

    public static CreativeTabs MAL_TAB;

    public static Block BLOCK_AUTO_CHEST;
    public static Block BLOCK_AUTO_FURNACE;
    public static Block BLOCK_AUTO_FURNACE_LIT;
    public static Block BLOCK_AUTO_FORESTER;
    public static Block BLOCK_AUTO_QUARRY;
    public static Block BLOCK_AUTO_WORKBENCH;

    public static Block BLOCK_DROID_HEAD;

    public static Item ITEM_CIRCUIT;

    public static SimpleNetworkWrapper NETWORK;

    public static MALConfig CONFIG;


    // This allows us to us one gui handler for many things
    public static enum GUI_ENUM {
        DROID, CHEST, FORESTER, FURNACE, QUARRY, WORKBENCH,  }

    @Instance(value = MALMain.MODID)
    public static MALMain instance;

    @SidedProxy(clientSide = "com.crashbox.mal.ClientProxy",
                serverSide = "com.crashbox.mal.ServerProxy")
    public static CommonProxy proxy;

    //----------------------------------------------------------------------------------------------

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        CONFIG = new MALConfig();
        CONFIG.loadAndInit(event.getSuggestedConfigurationFile());

        //_eventHandler = new MALEventHandler();
        //MinecraftForge.EVENT_BUS.register(_eventHandler);
        //FMLCommonHandler.instance().bus().register(_eventHandler);

        MAL_TAB = new CreativeTabMAL();
        preInitBlockAndItems();
        MALMain.NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel("mal");
        MALMain.NETWORK.registerMessage(MessageWorkDroidEffects.Handler.class,
                MessageWorkDroidEffects.class, 0, Side.CLIENT );

        proxy.preInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        // summon with: /summon mal.workDroid
        EntityRegistry.registerModEntity(EntityWorkDroid.class, "WorkDroid", ++modEntityID,
                MALMain.instance, 80, 3, false);

        NetworkRegistry.INSTANCE.registerGuiHandler(MALMain.instance,
                new GuiHandlerMAL());

        proxy.init(event);

        initRecipes();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        // Handle interaction with other mods, complete your setup based on this.
        proxy.postInit(event);
    }

    @EventHandler
    public void fmlLifeCycle(FMLServerStartingEvent event)
    {
        event.registerServerCommand(CONFIG.makeCommand());
    }

    @EventHandler
    public void fmlLifeCycle(FMLServerStartedEvent event)
    {
        World world = MinecraftServer.getServer().worldServerForDimension(0);

        Priority.setupGameRules(world);

        // Add other custom game rules
        GameRules rules = world.getGameRules();
    }

    //----------------------------------------------------------------------------------------------


    // PRIVATES
    private void preInitBlockAndItems()
    {
        BLOCK_AUTO_CHEST = new BlockAutoChest();
        GameRegistry.registerBlock(BLOCK_AUTO_CHEST, BlockAutoChest.NAME);
        GameRegistry.registerTileEntity(TileEntityAutoChest.class, TileEntityAutoChest.NAME);

        BLOCK_AUTO_FORESTER = new BlockAutoForester();
        GameRegistry.registerBlock(BLOCK_AUTO_FORESTER, BlockAutoForester.NAME);
        GameRegistry.registerTileEntity(TileEntityAutoForester.class, TileEntityAutoForester.NAME);

        BLOCK_AUTO_FURNACE = new BlockAutoFurnace(false);
        GameRegistry.registerBlock(BLOCK_AUTO_FURNACE, BlockAutoFurnace.NAME);
        GameRegistry.registerTileEntity(TileEntityAutoFurnace.class, TileEntityAutoFurnace.NAME);

        BLOCK_AUTO_FURNACE_LIT = new BlockAutoFurnace(true);
        GameRegistry.registerBlock(BLOCK_AUTO_FURNACE_LIT, BlockAutoFurnace.NAME_LIT);

        BLOCK_AUTO_QUARRY = new BlockAutoQuarry();
        GameRegistry.registerBlock(BLOCK_AUTO_QUARRY, BlockAutoQuarry.NAME);
        GameRegistry.registerTileEntity(TileEntityAutoQuarry.class, TileEntityAutoQuarry.NAME);

        BLOCK_AUTO_WORKBENCH = new BlockAutoWorkbench();
        GameRegistry.registerBlock(BLOCK_AUTO_WORKBENCH, BlockAutoWorkbench.NAME);
        GameRegistry.registerTileEntity(TileEntityAutoWorkbench.class, TileEntityAutoWorkbench.NAME);

        BLOCK_DROID_HEAD = new BlockWorkDroidHead();
        GameRegistry.registerBlock(BLOCK_DROID_HEAD, BlockWorkDroidHead.NAME);

        // ITEMS
        ITEM_CIRCUIT = new ItemCircuit();
        GameRegistry.registerItem(ITEM_CIRCUIT, ItemCircuit.NAME);
    }

    private void initRecipes()
    {
        GameRegistry.addRecipe(new ItemStack(ITEM_CIRCUIT),
                "GLG",
                "RSR",
                "GRG",
                'R', Items.redstone,
                'G', Items.gold_nugget,
                'L', new ItemStack(Items.dye, 0, 4),
                'S', Blocks.stone_slab
        );

        GameRegistry.addRecipe(new ItemStack(BLOCK_DROID_HEAD),
                "H",
                "C",
                "G",
                'H', Items.iron_helmet,
                'C', ITEM_CIRCUIT,
                'G', Blocks.glass_pane
        );

        GameRegistry.addRecipe(new ItemStack(BLOCK_AUTO_CHEST),
                "BCB",
                "-S-",
                "B-B",
                'S', Blocks.chest,
                'C', ITEM_CIRCUIT,
                'B', Blocks.iron_bars
        );

        GameRegistry.addRecipe(new ItemStack(BLOCK_AUTO_FORESTER),
                "BCB",
                "-S-",
                "B-B",
                'S', Items.iron_axe,
                'C', ITEM_CIRCUIT,
                'B', Blocks.iron_bars
        );

        GameRegistry.addRecipe(new ItemStack(BLOCK_AUTO_QUARRY),
                "BCB",
                "-S-",
                "B-B",
                'S', Items.iron_pickaxe,
                'C', ITEM_CIRCUIT,
                'B', Blocks.iron_bars
        );

        GameRegistry.addRecipe(new ItemStack(BLOCK_AUTO_FURNACE),
                "BCB",
                "-S-",
                "B-B",
                'S', Blocks.furnace,
                'C', ITEM_CIRCUIT,
                'B', Blocks.iron_bars
        );

        GameRegistry.addRecipe(new ItemStack(BLOCK_AUTO_WORKBENCH),
                "BCB",
                "-S-",
                "B-B",
                'S', Blocks.crafting_table,
                'C', ITEM_CIRCUIT,
                'B', Blocks.iron_bars
        );
    }

    private static int modEntityID;

    private MALEventHandler _eventHandler;

    public static final Logger LOGGER = LogManager.getLogger();
}

