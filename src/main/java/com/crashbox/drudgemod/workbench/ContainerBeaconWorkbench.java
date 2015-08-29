package com.crashbox.drudgemod.workbench;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class ContainerBeaconWorkbench extends Container
{
    /** The crafting matrix inventory (3x3). */
    public InventoryCrafting craftMatrix;
    public IInventory craftResult = new InventoryCraftResult();
    private TileEntityBeaconWorkbench _workbench;
    private World _world;
    private BlockPos _pos;

    public ContainerBeaconWorkbench(InventoryPlayer playerInventory, TileEntityBeaconWorkbench workbench)
    {
        _workbench = workbench;
        craftMatrix = _workbench.makeInventory(this);
        this._world = workbench.getWorld();
        this._pos = workbench.getPos();
        this.addSlotToContainer(new SlotCrafting(playerInventory.player, this.craftMatrix, this.craftResult, 0, 124, 35));
        int i;
        int j;

        // TODO:  Is this likely to cause race conditions because it is the same stack???  Should we copy?
        // Set them to the same stacks.
        LOGGER.debug("OPEN");
//        if (!this._world.isRemote)
//        {
            for (int x = 0; x < 9; ++x)
            {
                // Should we copy?
                if (_workbench._itemStacks[x] != null)
                    LOGGER.debug(x + ": " + Integer.toHexString(_workbench._itemStacks[x].hashCode()));
                else
                    LOGGER.debug(x + ": null");
                craftMatrix.setInventorySlotContents(x, workbench._itemStacks[x]);
            }
//        }

        for (i = 0; i < 3; ++i)
        {
            for (j = 0; j < 3; ++j)
            {
                this.addSlotToContainer(new Slot(this.craftMatrix, j + i * 3, 30 + j * 18, 17 + i * 18));
            }
        }

        for (i = 0; i < 3; ++i)
        {
            for (j = 0; j < 9; ++j)
            {
                this.addSlotToContainer(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (i = 0; i < 9; ++i)
        {
            this.addSlotToContainer(new Slot(playerInventory, i, 8 + i * 18, 142));
        }

        this.onCraftMatrixChanged(this.craftMatrix);

        LOGGER.debug("Constructed ContainerWorkbench");
    }

    /**
     * Callback for when the crafting matrix is changed.
     */
    public void onCraftMatrixChanged(IInventory inventoryIn)
    {
        this.craftResult.setInventorySlotContents(0, CraftingManager.getInstance().findMatchingRecipe(this.craftMatrix, this._world));
    }

    /**
     * Called when the container is closed.
     */
    public void onContainerClosed(EntityPlayer playerIn)
    {
        super.onContainerClosed(playerIn);

//        if (!this._world.isRemote)
//        {
//            for (int i = 0; i < 9; ++i)
//            {
//                ItemStack itemstack = this.craftMatrix.getStackInSlotOnClosing(i);
//
//                if (itemstack != null)
//                {
//                    playerIn.dropPlayerItemWithRandomChoice(itemstack, false);
//                }
//            }
//        }

//        if (!this._world.isRemote)
//        {
            LOGGER.debug("CLOSE");
            for (int x = 0; x < 9; ++x)
            {
                // We are removing it...
                //_workbench._itemStacks[x] = craftMatrix.getStackInSlot(x);
                _workbench._itemStacks[x] = craftMatrix.getStackInSlotOnClosing(x);
                if (_workbench._itemStacks[x] != null)
                    LOGGER.debug(x + ": " + Integer.toHexString(_workbench._itemStacks[x].hashCode()));
                else
                    LOGGER.debug(x + ": null");
            }
//        }

    }

    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return true;
//        return this._world.getBlockState(this._pos).getBlock() != Blocks.crafting_table ? false : playerIn.getDistanceSq((double) this._pos
//                .getX() + 0.5D, (double) this._pos.getY() + 0.5D, (double) this._pos.getZ() + 0.5D) <= 64.0D;
    }

    /**
     * Take a stack from the specified inventory slot.
     */
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index)
    {
        ItemStack itemstack = null;
        Slot slot = (Slot)this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (index == 0)
            {
                if (!this.mergeItemStack(itemstack1, 10, 46, true))
                {
                    return null;
                }

                slot.onSlotChange(itemstack1, itemstack);
            }
            else if (index >= 10 && index < 37)
            {
                if (!this.mergeItemStack(itemstack1, 37, 46, false))
                {
                    return null;
                }
            }
            else if (index >= 37 && index < 46)
            {
                if (!this.mergeItemStack(itemstack1, 10, 37, false))
                {
                    return null;
                }
            }
            else if (!this.mergeItemStack(itemstack1, 10, 46, false))
            {
                return null;
            }

            if (itemstack1.stackSize == 0)
            {
                slot.putStack((ItemStack)null);
            }
            else
            {
                slot.onSlotChanged();
            }

            if (itemstack1.stackSize == itemstack.stackSize)
            {
                return null;
            }

            slot.onPickupFromSlot(playerIn, itemstack1);
        }

        return itemstack;
    }

    /**
     * Called to determine if the current slot is valid for the stack merging (double-click) code. The stack passed in
     * is null for the initial slot that was double-clicked.
     */
    public boolean canMergeSlot(ItemStack p_94530_1_, Slot p_94530_2_)
    {
        return p_94530_2_.inventory != this.craftResult && super.canMergeSlot(p_94530_1_, p_94530_2_);
    }

    private static final Logger LOGGER = LogManager.getLogger();
}
