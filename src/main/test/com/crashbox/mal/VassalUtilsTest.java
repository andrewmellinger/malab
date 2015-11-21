package com.crashbox.mal;

import com.crashbox.mal.util.BlockWalker;
import com.crashbox.mal.util.VassalUtils;
import net.minecraft.util.BlockPos;
import org.junit.Test;

public class VassalUtilsTest
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

        System.out.println(VassalUtils.findIntersect(center, 3, new BlockPos(18, 0, 20)));
        System.out.println(VassalUtils.findIntersect(center, 3, new BlockPos(18, 0, -12)));

        System.out.println(VassalUtils.findIntersect(center, 3, new BlockPos(-8, 0, 20)));
        System.out.println(VassalUtils.findIntersect(center, 3, new BlockPos(-8, 0, -12)));

        System.out.println(VassalUtils.findIntersect(center, 3, new BlockPos(20, 0, 18)));
        System.out.println(VassalUtils.findIntersect(center, 3, new BlockPos(20, 0, -10)));

        System.out.println(VassalUtils.findIntersect(center, 3, new BlockPos(-10, 0, 18)));
        System.out.println(VassalUtils.findIntersect(center, 3, new BlockPos(-10, 0, -10)));

        // Cardinals
        System.out.println(VassalUtils.findIntersect(center, 3, new BlockPos(5, 0, 14)));
        System.out.println(VassalUtils.findIntersect(center, 3, new BlockPos(5, 0, -6)));

        System.out.println(VassalUtils.findIntersect(center, 3, new BlockPos(0, 0, 4)));
        System.out.println(VassalUtils.findIntersect(center, 3, new BlockPos(10, 0, 4)));
    }


    @Test
    public void testFirstDropOccurrence()
    {
//        BlockPos start = new BlockPos(10, 63, 45);
//        BlockPos end = new BlockPos(14, 59, 49);
//        VassalUtils.firstDropOccurrence(null, start, end, null);
    }

    @Test
    public void testFindClockwiseDirection()
    {
        // Walk around a block
        BlockPos center = new BlockPos(12,0,52);
        BlockWalker walker = new BlockWalker(new BlockPos(10,0,50),false, VassalUtils.COMPASS.EAST);
        for (int side = 0; side < 4; ++side)
        {
            for (int i = 0; i < 4; ++i)
            {
                walker.forward();
                System.out.println(VassalUtils.findClockwiseDir(center, walker.getPos()));
            }
            walker.turnRight();
        }
    }


}