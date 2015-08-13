package com.crashbox.drudgemod;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class DrudgeUtils
{
    public static void showStack()
    {
        try
        {
            throw new Exception("Arg");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
