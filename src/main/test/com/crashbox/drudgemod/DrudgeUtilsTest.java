package com.crashbox.drudgemod;

import net.minecraft.util.BlockPos;
import org.junit.Test;

import static org.junit.Assert.*;

public class DrudgeUtilsTest
{

    @Test
    public void testFindIntersect() throws Exception
    {
        BlockPos center = new BlockPos(5, 0, 4);
//        DrudgeUtils.findIntersect(center, 3, new BlockPos(20, 0, 9));
//
//        DrudgeUtils.findIntersect(center, 3, new BlockPos(1, 0, -20));
//
//        DrudgeUtils.findIntersect(center, 3, new BlockPos(5, 0, 20));
//
//        DrudgeUtils.findIntersect(center, 3, new BlockPos(1, 0, 6));

        System.out.println(DrudgeUtils.findIntersect(center, 3, new BlockPos(18, 0, 20)));
        System.out.println(DrudgeUtils.findIntersect(center, 3, new BlockPos(18, 0, -12)));

        System.out.println(DrudgeUtils.findIntersect(center, 3, new BlockPos(-8, 0, 20)));
        System.out.println(DrudgeUtils.findIntersect(center, 3, new BlockPos(-8, 0, -12)));

        System.out.println(DrudgeUtils.findIntersect(center, 3, new BlockPos(20, 0, 18)));
        System.out.println(DrudgeUtils.findIntersect(center, 3, new BlockPos(20, 0, -10)));

        System.out.println(DrudgeUtils.findIntersect(center, 3, new BlockPos(-10, 0, 18)));
        System.out.println(DrudgeUtils.findIntersect(center, 3, new BlockPos(-10, 0, -10)));
    }
}