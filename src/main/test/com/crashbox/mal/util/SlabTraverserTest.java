package com.crashbox.mal.util;

import net.minecraft.util.BlockPos;
import org.junit.Test;

public class SlabTraverserTest
{
    @Test
    public void testIteratorSouth() throws Exception
    {
        BlockPos center = new BlockPos(10,0,50);
        BlockPos starting = new BlockPos(12,0,52);
        SlabTraverser  traverser = new SlabTraverser(center, starting, 2, VassalUtils.COMPASS.SOUTH);
        for (BlockPos pos : traverser)
        {
            System.out.println(pos);
        }
    }

    @Test
    public void testIteratorWest() throws Exception
    {
        BlockPos center = new BlockPos(10,0,50);
        BlockPos starting = new BlockPos(8,0,52);
        SlabTraverser  traverser = new SlabTraverser(center, starting, 2, VassalUtils.COMPASS.WEST);

        System.out.println(traverser);
        System.out.println(traverser.iterator());

        for (BlockPos pos : traverser)
        {
            System.out.println(pos);
        }
    }
}