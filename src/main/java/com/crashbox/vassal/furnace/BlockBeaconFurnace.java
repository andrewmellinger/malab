package com.crashbox.vassal.furnace;

import com.crashbox.vassal.VassalMain;
import com.crashbox.vassal.util.VassalUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class BlockBeaconFurnace extends BlockContainer
{
    public static final String NAME = "beaconFurnace";
    public static final String NAME_LIT = "beaconFurnaceLit";

    public BlockBeaconFurnace(boolean lit)
    {
        super(Material.iron);
        setUnlocalizedName(VassalUtils.getLabeledName(NAME));

        if (!lit)
            setCreativeTab(VassalMain.VASSAL_TAB);
        setTickRandomly(false);

        setHardness(4.5F);
        setResistance(30);
        setStepSound(Block.soundTypePiston);
    }

    public static void setState(boolean active, World worldIn, BlockPos pos)
    {
        IBlockState iblockstate = worldIn.getBlockState(pos);
        TileEntity tileentity = worldIn.getTileEntity(pos);
        keepInventory = true;

        if (active)
        {
            worldIn.setBlockState(pos, VassalMain.BLOCK_BEACON_FURNACE_LIT.getDefaultState());
            worldIn.setBlockState(pos, VassalMain.BLOCK_BEACON_FURNACE_LIT.getDefaultState());
        }
        else
        {
            worldIn.setBlockState(pos, VassalMain.BLOCK_BEACON_FURNACE.getDefaultState());
            worldIn.setBlockState(pos, VassalMain.BLOCK_BEACON_FURNACE.getDefaultState());
        }

        keepInventory = false;

        if (tileentity != null)
        {
            tileentity.validate();
            worldIn.setTileEntity(pos, tileentity);
        }
    }


    @Override
    public TileEntity createNewTileEntity(World world, int i)
    {
        return new TileEntityBeaconFurnace();
    }

    @Override
    public void breakBlock(World inWorld, BlockPos inPos, IBlockState inBlockState)
    {
        if (!keepInventory)
        {
            TileEntity entity = inWorld.getTileEntity(inPos);
            if (entity instanceof TileEntityBeaconFurnace)
            {
                InventoryHelper.dropInventoryItems(inWorld, inPos,
                        (TileEntityBeaconFurnace) entity);
                inWorld.updateComparatorOutputLevel(inPos, this);
                ((TileEntityBeaconFurnace) entity).blockBroken();
            }
        }

        LOGGER.warn("hasTileEntity: " + hasTileEntity(inBlockState));
        LOGGER.warn("!!!! Removing tile entity: " + inPos);
        // This MUST be last because it removes the TileEntity.
        super.breakBlock(inWorld, inPos, inBlockState);
    }

    @Override
    public Item getItemDropped(
            IBlockState state,
            Random rand,
            int fortune)
    {
        return Item.getItemFromBlock(VassalMain.BLOCK_BEACON_FURNACE);
    }

    @Override
    public void onBlockAdded(
            World parWorld,
            BlockPos parBlockPos,
            IBlockState parIBlockState)
    {
    }

    @Override
    public boolean onBlockActivated(
            World parWorld,
            BlockPos parBlockPos,
            IBlockState parIBlockState,
            EntityPlayer entityPlayer,
            EnumFacing parSide,
            float hitX,
            float hitY,
            float hitZ)
    {
        if (!parWorld.isRemote)
        {
            // This triggers the general GuiHandler
            entityPlayer.openGui(VassalMain.instance,
                    VassalMain.GUI_ENUM.FURNACE.ordinal(),
                    parWorld,
                    parBlockPos.getX(),
                    parBlockPos.getY(),
                    parBlockPos.getZ());
        }

        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Item getItem(World worldIn, BlockPos pos)
    {
        return Item.getItemFromBlock(VassalMain.BLOCK_BEACON_FURNACE);
    }

    @Override
    public int getRenderType()
    {
        // We want the normal block renderer
        return 3;
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return 0;
    }

    private static boolean keepInventory = false;
    private static final Logger LOGGER = LogManager.getLogger();
}
