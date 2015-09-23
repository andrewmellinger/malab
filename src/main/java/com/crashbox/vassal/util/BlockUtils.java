package com.crashbox.vassal.util;

import net.minecraft.util.BlockPos;

/**
 * Copyright CMU 2015.
 */
public class BlockUtils
{
    public static BlockPos down(BlockPos pos)
    {
        return new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ());
    }

    public static BlockPos up(BlockPos pos)
    {
        return new BlockPos(pos.getX(), pos.getY() + 1, pos.getZ());
    }
}
