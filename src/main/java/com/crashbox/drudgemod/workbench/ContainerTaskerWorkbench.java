package com.crashbox.drudgemod.workbench;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class ContainerTaskerWorkbench extends ContainerWorkbench
{
    public ContainerTaskerWorkbench(InventoryPlayer playerInventory, World world, BlockPos pos, ItemStack[] items)
    {
        super(playerInventory, world, pos);
        _world = world;
        _stacks = items;

        for (int x = 0; x < 9; ++x)
        {
            this.craftMatrix.setInventorySlotContents(x, items[x]);
        }
    }

    /**
     * Called when the container is closed.
     */
    public void onContainerClosed(EntityPlayer playerIn)
    {
        // What do we need to do with the inventory?
        //super.onContainerClosed(playerIn);

        if (!_world.isRemote)
        {
            for (int i = 0; i < 9; ++i)
            {
                _stacks[i] = this.craftMatrix.getStackInSlotOnClosing(i);
            }
        }
    }


    private final ItemStack[] _stacks;
    private final World _world;

}
