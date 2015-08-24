package com.crashbox.drudgemod;

import com.crashbox.drudgemod.furnace.BlockTaskerFurnace;
import com.crashbox.drudgemod.furnace.TileEntityTaskerFurnace;
import com.crashbox.drudgemod.forester.BlockTaskerForester;
import com.crashbox.drudgemod.forester.TileEntityTaskerForester;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;


/**
 * Copyright 2015 Andrew o. Mellinger
 */
@Mod(modid= DrudgeMain.MODID, name= DrudgeMain.NAME, version= DrudgeMain.VERSION)
public class DrudgeMain
{
    // This guy talks about what each event handler does
    // http://greyminecraftcoder.blogspot.com/2013/11/how-forge-starts-up-your-code.html
    public static final String MODID = "drudgemod";
    public static final String NAME = "DrudgeMod";
    public static final String VERSION = "0.1.0";

    public static Block BLOCK_TASKER_FURNACE;
    public static Block BLOCK_TASKER_FORESTER;

    // This allows us to us one gui handler for many things
    public enum GUI_ENUM
    {
        FURNACE
    }

//    public static Item ITEM_SMART_BLOCK;

    // These are the blocks and items we load that other parts need to use.
    //public static ItemThrowableTorch ITEM_THROWABLE_TORCH;

    @Instance(value = DrudgeMain.MODID)
    public static DrudgeMain instance;

    @SidedProxy(clientSide = "com.crashbox.drudgemod.ClientProxy",
                serverSide = "com.crashbox.drudgemod.ServerProxy")
    public static CommonProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        preInitBlockAndItems();
        proxy.preInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        // Add entities
        int entityID = 0;
//        EntityRegistry.registerModEntity(EntityThrowableTorch.class, "Throwable Torch",
//                ++entityID, ModMain.instance, 80, 10, true);

//        GameRegistry.addRecipe(new ItemStack(ITEM_THROWABLE_TORCH),
//                "T",
//                "S",
//                'T', Blocks.torch,
//                'S', Items.slime_ball
//        );

        // summon with: /summon drudgemod.Drudge
        registerModEntityWithEgg(EntityDrudge.class, "Drudge", 0x3F5505, 0x4E6414);

        NetworkRegistry.INSTANCE.registerGuiHandler(DrudgeMain.instance,
                new GuiHandlerDrudge());

        proxy.init(event);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
//        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
//        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
//        dumpOreDict();
//        findLogWood();
//        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
//        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        // Handle interaction with other mods, complete your setup based on this.
        proxy.postInit(event);
    }

    public void registerModEntityWithEgg(Class parEntityClass, String parEntityName,
                                         int parEggColor, int parEggSpotsColor)
    {
        EntityRegistry.registerModEntity(parEntityClass, parEntityName, ++modEntityID,
                DrudgeMain.instance, 80, 3, false);
        //registerSpawnEgg(parEntityName, parEggColor, parEggSpotsColor);
    }

    // PRIVATES
    private void preInitBlockAndItems()
    {
        // TOO: in the future move this to the client proxy
        BLOCK_TASKER_FURNACE = new BlockTaskerFurnace();
        GameRegistry.registerBlock(BLOCK_TASKER_FURNACE, BlockTaskerFurnace.NAME);
        GameRegistry.registerTileEntity(TileEntityTaskerFurnace.class, TileEntityTaskerFurnace.NAME);

        BLOCK_TASKER_FORESTER = new BlockTaskerForester();
        GameRegistry.registerBlock(BLOCK_TASKER_FORESTER, BlockTaskerForester.NAME);
        GameRegistry.registerTileEntity(TileEntityTaskerForester.class, TileEntityTaskerForester.NAME);
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

    private static final Logger LOGGER = LogManager.getLogger();
}

