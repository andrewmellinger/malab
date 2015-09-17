package com.crashbox.vassal.ai;

import net.minecraft.util.BlockPos;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;

public class RingedSearcherTest
{
    @Test
    public void testRingedSearcher()
    {
        BlockPos center = new BlockPos(20, 1, 10);
        RingedSearcher rs = new RingedSearcher(center, 5, 4);

//        Iterator<BlockPos> iter = rs.iterator();
//        for ( int i =0; i < 125; ++i)
//        {
//            System.out.println(iter.next());
//        }

        for (BlockPos pos : rs)
        {
            System.out.println(pos);
        }
    }
}