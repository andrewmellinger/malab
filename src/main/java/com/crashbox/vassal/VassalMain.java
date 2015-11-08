package com.crashbox.vassal;

import com.crashbox.vassal.ai.Priority;
import com.crashbox.vassal.chest.BlockBeaconChest;
import com.crashbox.vassal.chest.TileEntityBeaconChest;
import com.crashbox.vassal.circuit.ItemCircuit;
import com.crashbox.vassal.entity.BlockVassalHead;
import com.crashbox.vassal.entity.EntityVassal;
import com.crashbox.vassal.furnace.BlockBeaconFurnace;
import com.crashbox.vassal.furnace.TileEntityBeaconFurnace;
import com.crashbox.vassal.forester.BlockBeaconForester;
import com.crashbox.vassal.forester.TileEntityBeaconForester;
import com.crashbox.vassal.network.MessageVassalEffects;
import com.crashbox.vassal.quarry.BlockBeaconQuarry;
import com.crashbox.vassal.quarry.TileEntityBeaconQuarry;
import com.crashbox.vassal.workbench.BlockBeaconWorkbench;
import com.crashbox.vassal.workbench.TileEntityBeaconWorkbench;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
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
@Mod(modid= VassalMain.MODID, name= VassalMain.NAME, version= VassalMain.VERSION)
public class VassalMain
{
    // This guy talks about what each event handler does
    // http://greyminecraftcoder.blogspot.com/2013/11/how-forge-starts-up-your-code.html
    public static final String MODID = "vassal";
    public static final String NAME = "Vassal";
    public static final String VERSION = "0.9.003";

    public static CreativeTabs VASSAL_TAB;

    public static Block BLOCK_BEACON_CHEST;
    public static Block BLOCK_BEACON_FURNACE;
    public static Block BLOCK_BEACON_FURNACE_LIT;
    public static Block BLOCK_BEACON_FORESTER;
    public static Block BLOCK_BEACON_QUARRY;
    public static Block BLOCK_BEACON_WORKBENCH;

    public static Block BLOCK_VASSAL_HEAD;

    public static Item ITEM_CIRCUIT;

    public static String GAME_RULE_NEXT_VASSAL_ID = "vassal.next.name.id";

    public static SimpleNetworkWrapper NETWORK;

    // This allows us to us one gui handler for many things
    public static enum GUI_ENUM { VASSAL, FURNACE, WORKBENCH, CHEST }

    @Instance(value = VassalMain.MODID)
    public static VassalMain instance;

    @SidedProxy(clientSide = "com.crashbox.vassal.ClientProxy",
                serverSide = "com.crashbox.vassal.ServerProxy")
    public static CommonProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        VASSAL_TAB = new CreativeTabVassal();
        preInitBlockAndItems();
        VassalMain.NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel("vassal");
        VassalMain.NETWORK.registerMessage(MessageVassalEffects.Handler.class,
                MessageVassalEffects.class, 0, Side.CLIENT );

        proxy.preInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        // summon with: /summon vassal.Vassal
        EntityRegistry.registerModEntity(EntityVassal.class, "Vassal", ++modEntityID,
                VassalMain.instance, 80, 3, false);

        NetworkRegistry.INSTANCE.registerGuiHandler(VassalMain.instance,
                new GuiHandlerVassal());

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
    public void fmlLifeCycle(FMLServerStartedEvent event)
    {
        World world = MinecraftServer.getServer().worldServerForDimension(0);

        Priority.setupGameRules(world);

        // Add other custom game rules
        GameRules rules = world.getGameRules();

        if (!rules.hasRule(GAME_RULE_NEXT_VASSAL_ID))
            rules.addGameRule(GAME_RULE_NEXT_VASSAL_ID, "1", GameRules.ValueType.NUMERICAL_VALUE);
    }

    // PRIVATES
    private void preInitBlockAndItems()
    {
        BLOCK_BEACON_CHEST = new BlockBeaconChest();
        GameRegistry.registerBlock(BLOCK_BEACON_CHEST, BlockBeaconChest.NAME);
        GameRegistry.registerTileEntity(TileEntityBeaconChest.class, TileEntityBeaconChest.NAME);

        BLOCK_BEACON_FORESTER = new BlockBeaconForester();
        GameRegistry.registerBlock(BLOCK_BEACON_FORESTER, BlockBeaconForester.NAME);
        GameRegistry.registerTileEntity(TileEntityBeaconForester.class, TileEntityBeaconForester.NAME);

        BLOCK_BEACON_FURNACE = new BlockBeaconFurnace(false);
        GameRegistry.registerBlock(BLOCK_BEACON_FURNACE, BlockBeaconFurnace.NAME);
        GameRegistry.registerTileEntity(TileEntityBeaconFurnace.class, TileEntityBeaconFurnace.NAME);

        BLOCK_BEACON_FURNACE_LIT = new BlockBeaconFurnace(true);
        GameRegistry.registerBlock(BLOCK_BEACON_FURNACE_LIT, BlockBeaconFurnace.NAME_LIT);
        //GameRegistry.registerTileEntity(TileEntityBeaconFurnace.class, TileEntityBeaconFurnace.NAME);

        BLOCK_BEACON_QUARRY = new BlockBeaconQuarry();
        GameRegistry.registerBlock(BLOCK_BEACON_QUARRY, BlockBeaconQuarry.NAME);
        GameRegistry.registerTileEntity(TileEntityBeaconQuarry.class, TileEntityBeaconQuarry.NAME);

        BLOCK_BEACON_WORKBENCH = new BlockBeaconWorkbench();
        GameRegistry.registerBlock(BLOCK_BEACON_WORKBENCH, BlockBeaconWorkbench.NAME);
        GameRegistry.registerTileEntity(TileEntityBeaconWorkbench.class, TileEntityBeaconWorkbench.NAME);

        BLOCK_VASSAL_HEAD = new BlockVassalHead();
        GameRegistry.registerBlock(BLOCK_VASSAL_HEAD, BlockVassalHead.NAME);

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

        GameRegistry.addRecipe(new ItemStack(BLOCK_VASSAL_HEAD),
                "H",
                "C",
                "G",
                'H', Items.iron_helmet,
                'C', ITEM_CIRCUIT,
                'G', Blocks.glass_pane
        );

        GameRegistry.addRecipe(new ItemStack(BLOCK_BEACON_CHEST),
                "BCB",
                "-S-",
                "B-B",
                'S', Blocks.chest,
                'C', ITEM_CIRCUIT,
                'B', Blocks.iron_bars
        );

        GameRegistry.addRecipe(new ItemStack(BLOCK_BEACON_FORESTER),
                "BCB",
                "-S-",
                "B-B",
                'S', Items.iron_axe,
                'C', ITEM_CIRCUIT,
                'B', Blocks.iron_bars
        );

        GameRegistry.addRecipe(new ItemStack(BLOCK_BEACON_QUARRY),
                "BCB",
                "-S-",
                "B-B",
                'S', Items.iron_pickaxe,
                'C', ITEM_CIRCUIT,
                'B', Blocks.iron_bars
        );

        GameRegistry.addRecipe(new ItemStack(BLOCK_BEACON_FURNACE),
                "BCB",
                "-S-",
                "B-B",
                'S', Blocks.furnace,
                'C', ITEM_CIRCUIT,
                'B', Blocks.iron_bars
        );

        GameRegistry.addRecipe(new ItemStack(BLOCK_BEACON_WORKBENCH),
                "BCB",
                "-S-",
                "B-B",
                'S', Blocks.crafting_table,
                'C', ITEM_CIRCUIT,
                'B', Blocks.iron_bars
        );
    }

    private static int modEntityID;

    public static final Logger LOGGER = LogManager.getLogger();
}

