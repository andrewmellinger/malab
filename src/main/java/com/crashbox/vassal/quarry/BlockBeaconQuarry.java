package com.crashbox.vassal.quarry;

import com.crashbox.vassal.VassalMain;
import com.crashbox.vassal.util.VassalUtils;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class BlockBeaconQuarry extends BlockContainer
{
    public static final String NAME = "beaconQuarry";

    public BlockBeaconQuarry()
    {
        super(Material.iron);
        setUnlocalizedName(VassalUtils.getLabeledName(NAME));
        setCreativeTab(VassalMain.VASSAL_TAB);
        setTickRandomly(false);

        // TOOD: Switch to iron, later.
        setHarvestLevel("pickaxe", 1);

        // We want to be a little harder so people to accidentally break them as much
        setHardness(5.0F);
        setResistance(45);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int i)
    {
        return new TileEntityBeaconQuarry();
    }

    @Override
    public void breakBlock(World inWorld, BlockPos inPos, IBlockState inBlockState)
    {
        TileEntity entity = inWorld.getTileEntity(inPos);
        if (entity instanceof TileEntityBeaconQuarry)
        {
            ((TileEntityBeaconQuarry)entity).blockBroken();
        }

        // This MUST be last because it removes the TileEntity.
        super.breakBlock(inWorld, inPos, inBlockState);
    }

    @Override
    public int getRenderType()
    {
        return 3;
    }

    private static final Logger LOGGER = LogManager.getLogger();
}
