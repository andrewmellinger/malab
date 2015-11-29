package com.crashbox.malab.forester;

import com.crashbox.malab.MALMain;
import com.crashbox.malab.util.MALUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class BlockAutoForester extends BlockContainer
{
    public static final String NAME = "forester";

    public BlockAutoForester()
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
        return new TileEntityAutoForester();
    }

    @Override
    public void breakBlock(World inWorld, BlockPos inPos, IBlockState inBlockState)
    {
        // TODO:  What does hasTileEntity do?  Do I really need this?
        if (hasTileEntity(inBlockState))
        {
            TileEntity entity = inWorld.getTileEntity(inPos);
            if (entity instanceof TileEntityAutoForester)
            {
                ((TileEntityAutoForester) entity).blockBroken();
            }
        }

        // This MUST be last because it removes the TileEntity.
        super.breakBlock(inWorld, inPos, inBlockState);
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
                    MALMain.GUI_ENUM.FORESTER.ordinal(),
                    parWorld,
                    parBlockPos.getX(),
                    parBlockPos.getY(),
                    parBlockPos.getZ());
        }

        return true;
    }


    @Override
    public int getRenderType()
    {
        return 3;
    }
}
