package com.crashbox.vassal;

import com.crashbox.vassal.chest.BlockBeaconChest;
import com.crashbox.vassal.chest.TileEntityBeaconChest;
import com.crashbox.vassal.entity.EntityVassal;
import com.crashbox.vassal.furnace.BlockBeaconFurnace;
import com.crashbox.vassal.furnace.TileEntityBeaconFurnace;
import com.crashbox.vassal.forester.BlockBeaconForester;
import com.crashbox.vassal.forester.TileEntityBeaconForester;
import com.crashbox.vassal.grenades.*;
import com.crashbox.vassal.network.MessageToggleWorkbenchEnable;
import com.crashbox.vassal.quarry.BlockBeaconQuarry;
import com.crashbox.vassal.quarry.TileEntityBeaconQuarry;
import com.crashbox.vassal.workbench.BlockBeaconWorkbench;
import com.crashbox.vassal.workbench.TileEntityBeaconWorkbench;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;


/**
 * Copyright 2015 Andrew o. Mellinger
 */
@Mod(modid= VassalMain.MODID, name= VassalMain.NAME, version= VassalMain.VERSION)
public class VassalMain
{
    // This guy talks about what each event handler does
    // http://greyminecraftcoder.blogspot.com/2013/11/how-forge-starts-up-your-code.html
    public static final String MODID = "vassal";
    public static final String NAME = "Vassal";
    public static final String VERSION = "0.1.0";

    public static CreativeTabs VASSAL_TAB;

    public static Block BLOCK_BEACON_FURNACE;
    public static Block BLOCK_BEACON_FURNACE_LIT;
    public static Block BLOCK_BEACON_FORESTER;
    public static Block BLOCK_BEACON_WORKBENCH;
    public static Block BLOCK_BEACON_CHEST;
    public static Block BLOCK_BEACON_QUARRY;

    public static Item ITEM_DIGGER_GRENADE;
    public static Item ITEM_MINESHAFT_GRENADE;
    public static Item ITEM_FLATTEN_GRENADE;
    public static Item ITEM_TUNNEL_GRENADE;

    //public static SimpleNetworkWrapper NETWORK;

    // This allows us to us one gui handler for many things
    public static enum GUI_ENUM { VASSAL, FURNACE, WORKBENCH, CHEST }

    // These are the blocks and items we load that other parts need to use.
    //public static ItemThrowableTorch ITEM_THROWABLE_TORCH;

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
//        VassalMain.NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel("vassal");
//        VassalMain.NETWORK.registerMessage(MessageToggleWorkbenchEnable.Handler.class,
//                MessageToggleWorkbenchEnable.class, 0, Side.SERVER );

        proxy.preInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        // summon with: /summon vassal.Vassal
        registerModEntityWithEgg(EntityVassal.class, "Vassal", 0x3F5505, 0x4E6414);

        EntityRegistry.registerModEntity(EntityDiggerGrenade.class, "Digger Grenade",
                ++modEntityID, VassalMain.instance, 80, 10, true);
        EntityRegistry.registerModEntity(EntityMineshaftGrenade.class, "Mineshaft Grenade",
                ++modEntityID, VassalMain.instance, 80, 10, true);
        EntityRegistry.registerModEntity(EntityFlattenGrenade.class, "Flatten Grenade",
                ++modEntityID, VassalMain.instance, 80, 10, true);
        EntityRegistry.registerModEntity(EntityTunnelGrenade.class, "Tunnel Grenade",
                ++modEntityID, VassalMain.instance, 80, 10, true);

        NetworkRegistry.INSTANCE.registerGuiHandler(VassalMain.instance,
                new GuiHandlerVassal());

        proxy.init(event);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        // Handle interaction with other mods, complete your setup based on this.
        proxy.postInit(event);

//        LOGGER.debug("****************************************************");
//        ItemStack stack = new ItemStack(Blocks.cobblestone);
//        LOGGER.debug("IronPick on cobblestone: " + Items.iron_pickaxe.getDigSpeed(stack,
//                Blocks.cobblestone.getDefaultState()));
//        LOGGER.debug("StonePick on cobblestone: " + Items.stone_pickaxe.getDigSpeed(stack,
//                Blocks.cobblestone.getDefaultState()));
//        LOGGER.debug("Cobblestone hardness: " + Blocks.cobblestone.getBlockHardness(null, null));
    }

    public void registerModEntityWithEgg(Class parEntityClass, String parEntityName,
                                         int parEggColor, int parEggSpotsColor)
    {
        EntityRegistry.registerModEntity(parEntityClass, parEntityName, ++modEntityID,
                VassalMain.instance, 80, 3, false);
        //registerSpawnEgg(parEntityName, parEggColor, parEggSpotsColor);
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

        // ITEMS

        ITEM_DIGGER_GRENADE = new ItemDiggerGrenade();
        GameRegistry.registerItem(ITEM_DIGGER_GRENADE, ItemDiggerGrenade.NAME);

        ITEM_MINESHAFT_GRENADE = new ItemMineshaftGrenade();
        GameRegistry.registerItem(ITEM_MINESHAFT_GRENADE, ItemMineshaftGrenade.NAME);

        ITEM_FLATTEN_GRENADE = new ItemFlattenGrenade();
        GameRegistry.registerItem(ITEM_FLATTEN_GRENADE, ItemFlattenGrenade.NAME);

        ITEM_TUNNEL_GRENADE = new ItemTunnelGrenade();
        GameRegistry.registerItem(ITEM_TUNNEL_GRENADE, ItemTunnelGrenade.NAME);
    }


    private void dumpOreDict()
    {
        for (String name : OreDictionary.getOreNames())
        {
            System.out.println("############# " + name + " -> " + OreDictionary.getOreID(name));
        }
    }

    private void findLogWood()
    {
        for (int meta = 0; meta < 4; ++meta)
        {
            ItemStack wood = new ItemStack(Blocks.log, 1, meta);
            System.out.println(wood + " -> " + Arrays.toString(OreDictionary.getOreIDs(wood)));
        }

        for (ItemStack ore : OreDictionary.getOres("logWood"))
        {
            System.out.println(ore);
        }

        ItemStack oak = new ItemStack(Blocks.log, 1, 0);
        ItemStack spruce = new ItemStack(Blocks.log, 1, 1);
        System.out.println("Strict match: " + OreDictionary.itemMatches(oak, spruce, true));
        System.out.println("UnStrict match: " + OreDictionary.itemMatches(oak, spruce, false));

        ItemStack yAxis = new ItemStack(Blocks.log, 1, 0);
        ItemStack xAxis = new ItemStack(Blocks.log, 1, 4);
        System.out.println("Strict match: " + OreDictionary.itemMatches(xAxis, yAxis, true));
        System.out.println("UnStrict match: " + OreDictionary.itemMatches(xAxis, yAxis, false));

    }

    private void initModEntities()
    {

//        // Do your mod setup. Build whatever data structures you care about. Register recipes.
//        int entityID = 0;
//        EntityRegistry.registerModEntity(EntityTanglerGrenadePlain.class, "Tangler Grenade",
//                ++entityID, TanglerMod.instance, 80, 10, true);
//        EntityRegistry.registerModEntity(EntityTanglerGrenadeBig.class, "Tangler Grenade Big",
//                ++entityID, TanglerMod.instance, 80, 10, true);
//        EntityRegistry.registerModEntity(EntityTanglerGrenadeGlow.class, "Tangler Grenade Glow",
//                ++entityID, TanglerMod.instance, 80, 10, true);
//        EntityRegistry.registerModEntity(EntityTanglerGrenadeTorch.class, "Tangler Grenade Torch",
//                ++entityID, TanglerMod.instance, 80, 10, true);
//        EntityRegistry.registerModEntity(EntityTanglerGrenadeHard.class, "Tangler Grenade Hard",
//                ++entityID, TanglerMod.instance, 80, 10, true);
//        EntityRegistry.registerModEntity(EntityTanglerGrenadeBoom.class, "Tangler Grenade Boom",
//                ++entityID, TanglerMod.instance, 80, 10, true);
//
//        initRecipes();
//
//        proxy.init(event);

    }


//
//    private void initRecipes()
//    {
//        // Basic tangler grenade
//        GameRegistry.addRecipe(new ItemStack(ITEM_TANGLER_GRENADE_PLAIN),
//                "-S-",
//                "SRS",
//                "-S-",
//                'R', Items.redstone,
//                'S', Items.slime_ball
//        );
//
//        GameRegistry.addRecipe(new ItemStack(ITEM_TANGLER_GRENADE_BIG),
//                "SSS",
//                "SRS",
//                "SSS",
//                'R', Items.redstone,
//                'S', Items.slime_ball
//        );
//
//        GameRegistry.addRecipe(new ItemStack(ITEM_TANGLER_GRENADE_TORCH),
//                "CS-",
//                "SRS",
//                "-S-",
//                'R', Items.redstone,
//                'S', Items.slime_ball,
//                'C', Items.coal
//        );
//
//        GameRegistry.addRecipe(new ItemStack(ITEM_TANGLER_GRENADE_TORCH),
//                "CS-",
//                "SRS",
//                "-S-",
//                'R', Items.redstone,
//                'S', Items.slime_ball,
//                'C', new ItemStack(Items.coal, 1, 1)
//        );
//
//        GameRegistry.addRecipe(new ItemStack(ITEM_TANGLER_GRENADE_GLOW),
//                "GS-",
//                "SRS",
//                "-S-",
//                'R', Items.redstone,
//                'S', Items.slime_ball,
//                'G', Items.glowstone_dust
//        );
//
//        GameRegistry.addRecipe(new ItemStack(ITEM_TANGLER_GRENADE_HARD),
//                "IS-",
//                "SRS",
//                "-S-",
//                'R', Items.redstone,
//                'S', Items.slime_ball,
//                'I', Items.iron_ingot
//        );
//
//        GameRegistry.addRecipe(new ItemStack(ITEM_TANGLER_GRENADE_BOOM),
//                "GS-",
//                "SRS",
//                "-S-",
//                'R', Items.redstone,
//                'S', Items.slime_ball,
//                'G', Items.gunpowder
//        );
//    }

    private static int modEntityID;

    public static final Logger LOGGER = LogManager.getLogger();
}

