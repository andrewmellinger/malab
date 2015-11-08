package com.crashbox.vassal.forester;

import com.crashbox.vassal.VassalMain;
import com.crashbox.vassal.util.VassalUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class BlockBeaconForester extends BlockContainer
{
    public static final String NAME = "beaconForester";

    public BlockBeaconForester()
    {
        super(Material.iron);
        setUnlocalizedName(VassalUtils.getLabeledName(NAME));
        setCreativeTab(VassalMain.VASSAL_TAB);
        setTickRandomly(false);

        setHardness(4.5F);
        setResistance(30);
        setStepSound(Block.soundTypePiston);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int i)
    {
        return new TileEntityBeaconForester();
    }

    @Override
    public void breakBlock(World inWorld, BlockPos inPos, IBlockState inBlockState)
    {
        // TODO:  What does hasTileEntity do?  Do I really need this?
        if (hasTileEntity(inBlockState))
        {
            TileEntity entity = inWorld.getTileEntity(inPos);
            if (entity instanceof TileEntityBeaconForester)
            {
                ((TileEntityBeaconForester) entity).blockBroken();
            }
        }

        // This MUST be last because it removes the TileEntity.
        super.breakBlock(inWorld, inPos, inBlockState);
    }

    @Override
    public int getRenderType()
    {
        return 3;
    }
}
