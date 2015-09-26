package com.crashbox.vassal.util;

import com.crashbox.vassal.VassalUtils;
import com.crashbox.vassal.common.AnyItemMatcher;
import com.crashbox.vassal.common.ItemStackMatcher;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStoneSlab;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemTool;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import scala.xml.dtd.ANY;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class StairBuilder
{
    // We are:
    // 1) At a landing, we want stairs below
    // 2) Below a landing and at stairs. We want a landing below
    // 3) At nothing, we want landing below

    // Basically we are working with the level below us.
    public StairBuilder(World world, BlockPos center, int radius)
    {
        _world = world;
        _center = center;
        _radius = radius;
        _landing = findLanding(world, center, radius);
        _dir = VassalUtils.findClockwiseDir(_center, _landing);
        _digCorner = getDigCorner();
        _slabList = makeSlabList(_dir);
    }

    // NOTE:  We are ONLY working below y
    public boolean findNextStair()
    {
        for (ProtoBlock proto : _slabList)
        {
            if (!_world.getBlockState(proto.getPos()).equals(proto.getState()))
            {
                _nextStair = proto.getPos();
                _nextState = proto.getState();
                return true;
            }
        }

        return false;
    }

    public BlockPos getStair()
    {
        return _nextStair;
    }

    public IBlockState getStairState()
    {
        return _nextState;
    }

    /**
     * Finds the first quarryable block that matches in the area.
     * @param matcher The matcher for the item we are looking for.
     * @param tool The tool we are using.
     * @return True for any.
     */
    public BlockPos findFirstQuarryable(ItemStackMatcher matcher, ItemTool tool)
    {

        LOGGER.debug(this);
        LOGGER.debug("findFirstQuarrayble: " + matcher);
        SlabTraverser traverser = new SlabTraverser(_center.down(), _digCorner, _radius, _dir);
        for (BlockPos pos : traverser)
        {
            // Skip air blocks
            if (_world.isAirBlock(pos))
            {
                LOGGER.debug("Skipping air block. pos=" + pos);
                continue;
            }

            // Should we skip it?
            if (isInExclusions(pos))
            {
                LOGGER.debug("Skipping in exclusions. pos=" + pos);
                continue;
            }

            //LOGGER.debug("Not in exclusion list: " + pos);
            // Can we break it?
            if (tool != null && !tool.canHarvestBlock(_world.getBlockState(pos).getBlock()))
            {
                LOGGER.debug("Can't harvest block with tool=" + tool + ", pos=" + pos);
                continue;
            }

            // Will it give us what we want?
            if (!(matcher instanceof AnyItemMatcher) && !VassalUtils.willDrop(_world, pos, matcher))
            {
                LOGGER.debug("Skipping, donsn't match desired type. pos=" + pos);
                continue;
            }

            // We are go for launch!
            LOGGER.debug("Found quarryable: " + pos);
            return pos;
        }
        return null;
    }

    /**
     * This is used to identify the block that is the landing.  From here we can find the rest of the
     * the info.  We look at the level we are on and one down.
     * @param world World obj
     * @param start We to start searching.
     * @param radius The radius of the area.
     * @return Corner with the landing.
     */
    public static BlockPos findLanding(World world, BlockPos start, int radius)
    {
        // Landing might be one above, at this level or one below.  We start down and work up
        IBlockState top = getTopSlab();

        // Try down one
        for (BlockPos pos : VassalUtils.getCorners(new BlockPos(start.getX(), start.getY() - 1,
                start.getZ()), radius))
        {
            if (world.getBlockState(pos).equals(top))
            {
                LOGGER.debug("Landing below: " + pos);
                return pos;
            }
        }

        // The landing might be at our level,
        for (BlockPos pos : VassalUtils.getCorners(start, radius))
        {
            if (world.getBlockState(pos).equals(top))
            {
                LOGGER.debug("Landing same: " + pos);
                return pos;
            }
        }

        // Try up one
        BlockPos[] corners = VassalUtils.getCorners(BlockUtils.up(start), radius);
        for (int i = 0; i < 4; ++i)
        {
            if (world.getBlockState(corners[i]).equals(top))
            {
                // Since we found one above, let's make one down.
                return corners[ (i + 1) % 4].down(2);
            }
        }

        for (BlockPos pos : VassalUtils.getCorners(new BlockPos(start.getX(), start.getY() + 1,
                start.getZ()), radius))
        {
            if (world.getBlockState(pos).equals(top))
            {
                LOGGER.debug("Landing above: " + pos);
                // We need to find the next one.
                return pos;
            }
        }

        // If there isn't one, choose NW corner
        BlockPos landing = new BlockPos(start.getX() - radius, start.getY() -1, start.getZ() - radius);
        LOGGER.debug("Couldn't find landing.  Assigning at :" + landing);
        return landing;
    }

    private BlockPos getDigCorner()
    {
        BlockPos center = _center.down();
        BlockPos corner = new BlockPos(_landing.getX(), center.getY(), _landing.getZ());
        return VassalUtils.nextCornerClockwise(center, corner);
    }

    private List<ProtoBlock> makeSlabList(VassalUtils.COMPASS dir)
    {
        LOGGER.debug("From landing stairs go: " + dir + " landing: "+ _landing);
        List<ProtoBlock> result = new ArrayList<ProtoBlock>();

        // If the landing is the same level, then one below is stairs
        if (_landing.getY() == _center.getY())
        {
            // Construct the walker one down
            BlockWalker walker = new BlockWalker(_landing.down(), false, dir);

            // Landing, is at our level, so return the stairs below
            walker.forward();
            walker.forward();
            walker.forward();
            for (BlockPos pos : walker.getRow(0, 1))
                result.add(new ProtoBlock(pos, getTopSlab()));
            walker.forward();
            for (BlockPos pos : walker.getRow(0, 1))
                result.add(new ProtoBlock(pos, getBottomSlab()));
        }
        else
        {
            BlockWalker walker = new BlockWalker(_landing, false, dir);

            // Landing is below, so just show those.
            for (BlockPos pos : walker.getRow(0, 1))
                result.add(new ProtoBlock(pos, getTopSlab()));
            walker.forward();
            for (BlockPos pos : walker.getRow(0, 1))
                result.add(new ProtoBlock(pos, getTopSlab()));
            walker.forward();
            for (BlockPos pos : walker.getRow(0, 1))
                result.add(new ProtoBlock(pos, getBottomSlab()));
        }

        return result;
    }


    public static IBlockState getTopSlab()
    {
        return Blocks.stone_slab.getDefaultState().
                withProperty(BlockStoneSlab.VARIANT, BlockStoneSlab.EnumType.COBBLESTONE).
                withProperty(BlockStoneSlab.HALF, BlockSlab.EnumBlockHalf.TOP);
    }

    public static IBlockState getBottomSlab()
    {
        return Blocks.stone_slab.getDefaultState().
                withProperty(BlockStoneSlab.VARIANT, BlockStoneSlab.EnumType.COBBLESTONE).
                withProperty(BlockStoneSlab.HALF, BlockSlab.EnumBlockHalf.BOTTOM);

    }

    private boolean isInExclusions(BlockPos pos)
    {
        for (ProtoBlock ex : _slabList)
        {
            if (pos.equals(ex.getPos()))
            {
                return true;
            }
        }
        return false;
    }

    private void dumpExclusionList()
    {
        for (ProtoBlock proto : _slabList)
            LOGGER.debug("+++++++>>>>>>>>>>" +proto.getPos());
    }

    @Override
    public String toString()
    {
        return "StairBuilder{" +
                "_center=" + _center +
                ", _radius=" + _radius +
                ", _landing=" + _landing +
                ", _digCorner=" + _digCorner +
                ", _slabList=" + _slabList +
                ", _nextStair=" + _nextStair +
                ", _nextState=" + _nextState +
                '}';
    }

    private World _world;
    private BlockPos _center;
    private int _radius;
    private BlockPos _landing;
    private BlockPos _digCorner;
    private VassalUtils.COMPASS _dir;

    private List<ProtoBlock> _slabList = new ArrayList<ProtoBlock>();

    private BlockPos _nextStair;
    private IBlockState _nextState;

    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();

}
