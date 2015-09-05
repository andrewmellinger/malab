package com.crashbox.drudgemod.util;

import net.minecraft.util.BlockPos;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class WorkArea
{
    public WorkArea(BlockPos center, int radius)
    {
        _center = center;
        _radius = radius;
    }

    private final BlockPos _center;
    private final int _radius;
}
