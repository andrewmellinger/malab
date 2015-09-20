package com.crashbox.vassal.util;

import com.crashbox.vassal.VassalUtils;
import com.crashbox.vassal.ai.AIUtils;
import com.crashbox.vassal.common.ItemStackMatcher;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStoneSlab;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class StairBuilder
{
    // Should we just remove blocks?
    // And have a stair placement delivery?
    // If we just convert things, then we might not have something to convert.
    // How can we get it to mine and place?
    // Does the quarry say "I want stairs" and issue a stair placement?
    // Does it then look for stone?  Could it bring stone from somewhere else?



    // We are:
    // 1) At a landing, we want stairs below
    // 2) Below a landing and at stairs. We want a landing below
    // 3) Nothing.




    // Basically we are working with the level below us.

    public StairBuilder(World world, BlockPos center, int radius)
    {
        _world = world;
        _center = center;
        _radius = radius;
        _landing = findLanding(world, center, radius);
        _landingBelow = (_landing.getY() != center.getY());
        _exclusions = makeExclusionList();
    }

    // NOTE:  We are ONLY working below y
    public boolean findNextStair()
    {
        if (_landing.getY() == _center.getY())
        {
            VassalUtils.COMPASS dir = VassalUtils.findClockwiseDir(_center, _landing);
            BlockWalker walker = new BlockWalker(_landing, false, dir);

            BlockPos[] row = walker.getRow(0, 1);
            for (BlockPos pos : row)
            {
                if (!_world.getBlockState(pos).equals(getTopSlab()))
                {
                    _nextStair = pos;
                    _nextState = getTopSlab();
                    return true;
                }
            }
            walker.forward();
        }
        else
        {
            // Look for up and down at right place
        }

        return false;
    }

    public BlockPos getStair()
    {
        return null;
    }

    public IBlockState getStairState()
    {
        return null;
    }

    public BlockPos findFirstQuarryable(ItemStackMatcher matcher)
    {
        SlabTraverser traverser = new SlabTraverser(_center, _radius);
        for (BlockPos pos : traverser)
        {
            if (VassalUtils.willDrop(_world, pos, matcher))
            {
                if (!isInExclusions(pos))
                    return pos;
            }
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
                return pos;
            }
        }

        // The landing might be at our level,
        for (BlockPos pos : VassalUtils.getCorners(start, radius))
        {
            if (world.getBlockState(pos).equals(top))
            {
                return pos;
            }
        }

        // Try down one
        for (BlockPos pos : VassalUtils.getCorners(new BlockPos(start.getX(), start.getY() + 1,
                start.getZ()), radius))
        {
            if (world.getBlockState(pos).equals(top))
            {
                return pos;
            }
        }

        // None here.
        return null;
    }

    private List<BlockPos> makeExclusionList()
    {
        VassalUtils.COMPASS dir = VassalUtils.findClockwiseDir(_center, _landing);
        BlockWalker walker = new BlockWalker(_landing, false, dir);
        List<BlockPos> result = new ArrayList<BlockPos>();

        if (_landingBelow)
        {
            // Landing, is at our level, so return the stairs below
            walker.forward();
            walker.forward();
            walker.forward();
            result.addAll(Arrays.asList(walker.getRow(0, 1)));
            walker.forward();
            result.addAll(Arrays.asList(walker.getRow(0, 1)));
        }
        else
        {
            // Landing is below, so just show those.
            result.addAll(Arrays.asList(walker.getRow(0, 1)));
            walker.forward();
            result.addAll(Arrays.asList(walker.getRow(0, 1)));
            walker.forward();
            result.addAll(Arrays.asList(walker.getRow(0, 1)));
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
        for (BlockPos ex : _exclusions)
        {
            if (pos.equals(ex))
            {
                return true;
            }
        }
        return false;
    }

    private World _world;
    private BlockPos _center;
    private int _radius;
    private BlockPos _landing;
    private boolean _landingBelow = true;


    private List<BlockPos> _exclusions;


    private BlockPos _nextStair;
    private IBlockState _nextState;
}
