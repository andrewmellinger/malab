package com.crashbox.drudgemod.furnace;

import com.crashbox.drudgemod.DrudgeMain;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
public class BlockTaskerFurnace extends BlockContainer
{
//    public static final PropertyBool TRIGGERED = PropertyBool.create("triggered");

    public static final PropertyDirection FACING =
        PropertyDirection.create("facing",
                EnumFacing.Plane.HORIZONTAL);

    public static final String NAME = "taskerFurnace";

    //TODO: In the future this should be protected
    public BlockTaskerFurnace()
    {
        super(Material.iron);
        setUnlocalizedName(DrudgeMain.MODID + "_" + NAME);
        setCreativeTab(CreativeTabs.tabRedstone);

        setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
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
        LOGGER.debug("++++++ newTileEntity: " + world.isRemote);
        return new TileEntityTaskerFurnace();
    }

    @Override
    public void breakBlock(World inWorld, BlockPos inPos, IBlockState inBlockState)
    {
        // TODO:  What does hasTileEntity do?  Do I really need this?
//        if (hasTileEntity(inBlockState))
//        {
//            TileEntity entity = inWorld.getTileEntity(inPos);
//            if (entity instanceof TileEntityTaskerFurnace)
//            {
//                ((TileEntityTaskerFurnace)entity).terminate();
//            }
//        }

        // This MUST be last because it removes the TileEntity.
        super.breakBlock(inWorld, inPos, inBlockState);
    }

    @Override
    public Item getItemDropped(
            IBlockState state,
            Random rand,
            int fortune)
    {
        return Item.getItemFromBlock(DrudgeMain.BLOCK_TASKER_FURNACE);
    }

    @Override
    public void onBlockAdded(
            World parWorld,
            BlockPos parBlockPos,
            IBlockState parIBlockState)
    {
//        if (!parWorld.isRemote)
//        {
//            // Rotate block if the front side is blocked
//            Block blockToNorth = parWorld.getBlockState(
//                    parBlockPos.offset(EnumFacing.NORTH)).getBlock();
//            Block blockToSouth = parWorld.getBlockState(
//                    parBlockPos.offset(EnumFacing.SOUTH)).getBlock();
//            Block blockToWest = parWorld.getBlockState(
//                    parBlockPos.offset(EnumFacing.WEST)).getBlock();
//            Block blockToEast = parWorld.getBlockState(
//                    parBlockPos.offset(EnumFacing.EAST)).getBlock();
//
//            EnumFacing enumfacing = (EnumFacing)parIBlockState
//                    .getValue(FACING);
//
//            if (enumfacing == EnumFacing.NORTH
//                    && blockToNorth.isFullBlock()
//                    && !blockToSouth.isFullBlock())
//            {
//                enumfacing = EnumFacing.SOUTH;
//            }
//            else if (enumfacing == EnumFacing.SOUTH
//                    && blockToSouth.isFullBlock()
//                    && !blockToNorth.isFullBlock())
//            {
//                enumfacing = EnumFacing.NORTH;
//            }
//            else if (enumfacing == EnumFacing.WEST
//                        && blockToWest.isFullBlock()
//                        && !blockToEast.isFullBlock())
//                {
//                    enumfacing = EnumFacing.EAST;
//                }
//                else if (enumfacing == EnumFacing.EAST
//                            && blockToEast.isFullBlock()
//                            && !blockToWest.isFullBlock())
//                    {
//                        enumfacing = EnumFacing.WEST;
//                    }
//
//            parWorld.setBlockState(parBlockPos, parIBlockState
//                    .withProperty(FACING, enumfacing), 2);
//        }
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
            // I think this invokes the GUIHandlerTaskerFurnace
            entityPlayer.openGui(DrudgeMain.instance,
                    DrudgeMain.GUI_ENUM.FURNACE.ordinal(),
                    parWorld,
                    parBlockPos.getX(),
                    parBlockPos.getY(),
                    parBlockPos.getZ());
        }

        return true;
    }

    @Override
    public IBlockState onBlockPlaced(
            World worldIn,
            BlockPos pos,
            EnumFacing facing,
            float hitX,
            float hitY,
            float hitZ,
            int meta,
            EntityLivingBase placer)
    {
        return getDefaultState().withProperty(FACING, EnumFacing.NORTH);
//        return getDefaultState().withProperty(FACING,
//                placer.func_174811_aO().getOpposite());
    }

    @Override
    public void onBlockPlacedBy(
            World worldIn,
            BlockPos pos,
            IBlockState state,
            EntityLivingBase placer,
            ItemStack stack)
    {
        worldIn.setBlockState(pos, state.withProperty(FACING, EnumFacing.NORTH), 2);
//        worldIn.setBlockState(pos, state.withProperty(
//                        FACING,
//                        placer.func_174811_aO().getOpposite()),
//                2);
    }

//    @Override
//    public void breakBlock(
//            World worldIn,
//            BlockPos pos,
//            IBlockState state)
//    {
//        if (!hasTileEntity)
//        {
//            TileEntity tileentity = worldIn.getTileEntity(pos);
//
//            if (tileentity instanceof TileEntityGrinder)
//            {
//                InventoryHelper.dropInventoryItems(worldIn, pos,
//                        (TileEntityGrinder)tileentity);
//                worldIn.updateComparatorOutputLevel(pos, this);
//            }
//        }
//
//        super.breakBlock(worldIn, pos, state);
//    }

    @Override
    @SideOnly(Side.CLIENT)
    public Item getItem(World worldIn, BlockPos pos)
    {
        return Item.getItemFromBlock(DrudgeMain.BLOCK_TASKER_FURNACE);
    }

    @Override
    public int getRenderType()
    {
        return 3;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IBlockState getStateForEntityRender(IBlockState state)
    {
        return getDefaultState().withProperty(FACING, EnumFacing.SOUTH);
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        EnumFacing enumfacing = EnumFacing.getFront(meta);

        if (enumfacing.getAxis() == EnumFacing.Axis.Y)
        {
            enumfacing = EnumFacing.NORTH;
        }

        return getDefaultState().withProperty(FACING, enumfacing);
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return ((EnumFacing)state.getValue(FACING)).getIndex();
    }

    @Override
    protected BlockState createBlockState()
    {
        return new BlockState(this, new IProperty[] {FACING});
    }

//    @SideOnly(Side.CLIENT)
//    static final class SwitchEnumFacing
//    {
//        static final int[] enumFacingArray = new int[EnumFacing.values()
//                .length];
//
//        static
//        {
//            try
//            {
//                enumFacingArray[EnumFacing.WEST.ordinal()] = 1;
//            }
//            catch (NoSuchFieldError var4)
//            {
//                ;
//            }
//
//            try
//            {
//                enumFacingArray[EnumFacing.EAST.ordinal()] = 2;
//            }
//            catch (NoSuchFieldError var3)
//            {
//                ;
//            }
//
//            try
//            {
//                enumFacingArray[EnumFacing.NORTH.ordinal()] = 3;
//            }
//            catch (NoSuchFieldError var2)
//            {
//                ;
//            }
//
//            try
//            {
//                enumFacingArray[EnumFacing.SOUTH.ordinal()] = 4;
//            }
//            catch (NoSuchFieldError var1)
//            {
//                // You should improve the error handling here
//            }
//        }
//    }

    private static final Logger LOGGER = LogManager.getLogger();
}
