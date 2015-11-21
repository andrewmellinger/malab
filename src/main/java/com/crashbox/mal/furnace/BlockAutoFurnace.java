package com.crashbox.mal.furnace;

import com.crashbox.mal.MALMain;
import com.crashbox.mal.util.MALUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


import java.util.Random;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class BlockAutoFurnace extends BlockContainer
{
    public static final String NAME = "furnace";
    public static final String NAME_LIT = "furnaceLit";

    public BlockAutoFurnace(boolean lit)
    {
        super(Material.iron);
        setUnlocalizedName(MALUtils.getLabeledName(NAME));

        if (!lit)
            setCreativeTab(MALMain.MAL_TAB);
        setTickRandomly(false);

        setHardness(4.5F);
        setResistance(30);
        setStepSound(Block.soundTypePiston);
    }

    public static void setState(boolean active, World worldIn, BlockPos pos)
    {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        keepInventory = true;

        if (active)
        {
            worldIn.setBlockState(pos, MALMain.BLOCK_AUTO_FURNACE_LIT.getDefaultState());
            worldIn.setBlockState(pos, MALMain.BLOCK_AUTO_FURNACE_LIT.getDefaultState());
        }
        else
        {
            worldIn.setBlockState(pos, MALMain.BLOCK_AUTO_FURNACE.getDefaultState());
            worldIn.setBlockState(pos, MALMain.BLOCK_AUTO_FURNACE.getDefaultState());
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
        return new TileEntityAutoFurnace();
    }

    @Override
    public void breakBlock(World inWorld, BlockPos inPos, IBlockState inBlockState)
    {
        if (!keepInventory)
        {
            TileEntity entity = inWorld.getTileEntity(inPos);
            if (entity instanceof TileEntityAutoFurnace)
            {
                InventoryHelper.dropInventoryItems(inWorld, inPos,
                        (TileEntityAutoFurnace) entity);
                inWorld.updateComparatorOutputLevel(inPos, this);
                ((TileEntityAutoFurnace) entity).blockBroken();
            }
        }

        // This MUST be last because it removes the TileEntity.
        super.breakBlock(inWorld, inPos, inBlockState);
    }

    @Override
    public Item getItemDropped(
            IBlockState state,
            Random rand,
            int fortune)
    {
        return Item.getItemFromBlock(MALMain.BLOCK_AUTO_FURNACE);
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
            entityPlayer.openGui(MALMain.instance,
                    MALMain.GUI_ENUM.FURNACE.ordinal(),
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
        return Item.getItemFromBlock(MALMain.BLOCK_AUTO_FURNACE);
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
}
