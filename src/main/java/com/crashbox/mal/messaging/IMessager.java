package com.crashbox.mal.messaging;

import net.minecraft.util.BlockPos;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public interface IMessager
{
    public BlockPos getBlockPos();

    public int getRadius();
}
