package com.crashbox.vassal.grenades;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class ItemMineshaftGrenade extends Item
{
    public static String NAME = "mineshaftGrenade";

    public ItemMineshaftGrenade()
    {
        setMaxStackSize(64);
        setCreativeTab(CreativeTabs.tabTools);

        setUnlocalizedName(NAME);
    }

    /**
     * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
     */
    public ItemStack onItemRightClick(ItemStack par1ItemStack, World world, EntityPlayer entityPlayer)
    {
        if (!entityPlayer.capabilities.isCreativeMode)
        {
            par1ItemStack.stackSize--;
        }

        if (!world.isRemote)
        {
            world.spawnEntityInWorld(new EntityMineshaftGrenade(world, entityPlayer));
        }

        return par1ItemStack;
    }

}
