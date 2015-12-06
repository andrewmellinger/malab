package com.crashbox.malab.workdroid;

import org.junit.Test;

public class EntityWorkDroidTest
{
    @Test
    public void testName()
    {
        for (int i = 0; i < 10; ++i)
        {
            System.out.println(EntityWorkDroid.makeName());
        }
    }
}