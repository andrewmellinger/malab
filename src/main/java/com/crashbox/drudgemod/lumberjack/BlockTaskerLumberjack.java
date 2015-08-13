package com.crashbox.drudgemod.lumberjack;

import com.crashbox.drudgemod.DrudgeMain;
import com.crashbox.drudgemod.lumberjack.TileEntityTaskerLumberjack;
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
public class BlockTaskerLumberjack extends BlockContainer
{
    public static final String NAME = "taskerLumberjack";

    public BlockTaskerLumberjack()
    {
        super(Material.iron);
        setUnlocalizedName(DrudgeMain.MODID + "_" + NAME);
        setCreativeTab(CreativeTabs.tabRedstone);

//        setDefaultState(blockState.getBaseState().withProperty(
//                FACING, EnumFacing.NORTH));
        stepSound = soundTypeSnow;
        blockParticleGravity = 1.0F;
        slipperiness = 0.6F;
        setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        lightOpacity = 20; // cast a light shadow
        setTickRandomly(false);
        useNeighborBrightness = false;

        LOGGER.info("constructed");
    }

    @Override
    public TileEntity createNewTileEntity(World world, int i)
    {
        return new TileEntityTaskerLumberjack();
    }

    @Override
    public void breakBlock(World inWorld, BlockPos inPos, IBlockState inBlockState)
    {
        // TODO:  What does hasTileEntity do?  Do I really need this?
        if (hasTileEntity(inBlockState))
        {
            TileEntity entity = inWorld.getTileEntity(inPos);
            if (entity instanceof TileEntityTaskerLumberjack)
            {
                ((TileEntityTaskerLumberjack)entity).blockBroken();
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

    private static final Logger LOGGER = LogManager.getLogger();
}
