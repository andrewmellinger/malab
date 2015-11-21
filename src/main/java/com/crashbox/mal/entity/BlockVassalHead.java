package com.crashbox.mal.entity;

import com.crashbox.mal.VassalMain;
import com.crashbox.mal.util.VassalUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class BlockVassalHead extends Block
{
    public static final String NAME = "vassalHead";

    public BlockVassalHead()
    {
        super(Material.iron);
        setUnlocalizedName(VassalUtils.getLabeledName(NAME));
        setCreativeTab(VassalMain.VASSAL_TAB);
        setTickRandomly(false);

        setHardness(1.5F);
        setResistance(10);
        setStepSound(Block.soundTypePiston);
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state)
    {
        // Look for blocks below and spawn vassal
        BlockPos down1 = pos.down();
        BlockPos down2 = pos.down(2);

        if (worldIn.getBlockState(down1).getBlock() == Blocks.furnace &&
                worldIn.getBlockState(down2).getBlock() == Blocks.piston)
        {
            worldIn.destroyBlock(pos, false);
            worldIn.destroyBlock(down1, false);
            worldIn.destroyBlock(down2, false);

            EntityVassal entityVassal = new EntityVassal(worldIn);
            entityVassal.setUpCustomName();
            entityVassal.setLocationAndAngles((double) down2.getX() + 0.5D,
                    (double) down2.getY() + 0.05D,
                    (double) down2.getZ() + 0.5D,
                    0.0F,
                    0.0F);
            worldIn.spawnEntityInWorld(entityVassal);
        }
    }
}
