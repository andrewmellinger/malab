package com.crashbox.malab.chest;

import com.crashbox.malab.MALMain;
import com.crashbox.malab.util.MALUtils;
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
public class BlockAutoChest extends BlockContainer
{
    public static final String NAME = "chest";

    public BlockAutoChest()
    {
        super(Material.iron);
        setUnlocalizedName(MALUtils.getLabeledName(NAME));
        setCreativeTab(MALMain.MAL_TAB);
        setTickRandomly(false);

        setHardness(4.5F);
        setResistance(30);
        setStepSound(Block.soundTypePiston);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int i)
    {
        return new TileEntityAutoChest();
    }

    @Override
    public void breakBlock(World inWorld, BlockPos inPos, IBlockState inBlockState)
    {
        if (hasTileEntity(inBlockState))
        {
            TileEntity entity = inWorld.getTileEntity(inPos);
            if (entity instanceof TileEntityAutoChest)
            {
                InventoryHelper.dropInventoryItems(inWorld, inPos,
                        (TileEntityAutoChest) entity);
                inWorld.updateComparatorOutputLevel(inPos, this);
                ((TileEntityAutoChest)entity).blockBroken();
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
        return Item.getItemFromBlock(MALMain.BLOCK_AUTO_CHEST);
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
                    MALMain.GUI_ENUM.CHEST.ordinal(),
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
        return Item.getItemFromBlock(MALMain.BLOCK_AUTO_CHEST);
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
}
