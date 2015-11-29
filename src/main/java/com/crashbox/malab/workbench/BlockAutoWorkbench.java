package com.crashbox.malab.workbench;

import com.crashbox.malab.MALabMain;
import com.crashbox.malab.util.MALUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
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
public class BlockAutoWorkbench extends BlockContainer
{
    public static final String NAME = "workbench";

    public BlockAutoWorkbench()
    {
        super(Material.iron);
        setUnlocalizedName(MALUtils.getLabeledName(NAME));
        setCreativeTab(MALabMain.MAL_TAB);
        setTickRandomly(false);

        setHardness(4.5F);
        setResistance(30);
        setStepSound(Block.soundTypePiston);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int i)
    {
        return new TileEntityAutoWorkbench();
    }

    @Override
    public void breakBlock(World inWorld, BlockPos inPos, IBlockState inBlockState)
    {
        TileEntity entity = inWorld.getTileEntity(inPos);
        if (entity instanceof TileEntityAutoWorkbench)
        {
            ((TileEntityAutoWorkbench)entity).blockBroken();
            inWorld.updateComparatorOutputLevel(inPos, this);
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
        return Item.getItemFromBlock(MALabMain.BLOCK_AUTO_WORKBENCH);
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
            entityPlayer.openGui(MALabMain.instance,
                    MALabMain.GUI_ENUM.WORKBENCH.ordinal(),
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
        return Item.getItemFromBlock(MALabMain.BLOCK_AUTO_WORKBENCH);
    }

    @Override
    public int getRenderType()
    {
        return 3;
    }

    private static final Logger LOGGER = LogManager.getLogger();
}
