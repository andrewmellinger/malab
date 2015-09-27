package com.crashbox.vassal.workbench;

import com.crashbox.vassal.VassalUtils;
import com.crashbox.vassal.ai.SlotOutput;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class ContainerBeaconWorkbench extends Container
{
    public ContainerBeaconWorkbench(InventoryPlayer playerInventory, TileEntityBeaconWorkbench workbench)
    {
        _workbench = workbench;

        // The tile entity keeps a persistent crafting core that we wrap.
        //_craftMatrix = _workbench.makeInventory(this);
        _craftingCore = _workbench.getCraftingCore();
        _craftMatrix = _craftingCore.getCraftingMatrix();
        _craftResult = _craftingCore.getCraftResult();
        _craftOutput = _craftingCore.getCraftOutput();

        // Crafting result slot
        this.addSlotToContainer(new SlotCrafting(playerInventory.player, _craftMatrix, _craftResult,
                0, 93, 35));

        // Output slot
        addSlotToContainer(new SlotOutput(playerInventory.player, _craftOutput, 0, 147, 35 ));

        // ===========================
        int i;
        int j;

        // Add all this things to the container
        for (i = 0; i < 3; ++i)
        {
            for (j = 0; j < 3; ++j)
            {
                this.addSlotToContainer(new Slot(this._craftMatrix, j + i * 3, 8 + j * 18, 17 + i * 18));
            }
        }

        // ===========================
        // Player inventory
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

        this.onCraftMatrixChanged(this._craftMatrix);

        LOGGER.debug("Constructed ContainerWorkbench");
    }

    /**
     * Callback for when the crafting matrix is changed.
     */
    public void onCraftMatrixChanged(IInventory inventoryIn)
    {
        _craftingCore.onCraftMatrixChanged(inventoryIn);
//        this._craftResult.setInventorySlotContents(0,
//                CraftingManager.getInstance().findMatchingRecipe(this._craftMatrix, this._world));
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
//                ItemStack itemstack = this._craftMatrix.getStackInSlotOnClosing(i);
//
//                if (itemstack != null)
//                {
//                    playerIn.dropPlayerItemWithRandomChoice(itemstack, false);
//                }
//            }
//        }
    }

    @Override
    public void addCraftingToCrafters(ICrafting listener)
    {
        super.addCraftingToCrafters(listener);
        listener.func_175173_a(this, _workbench);
    }

    /**
     * Looks for changes made in the container, sends them to every listener.
     */
    @Override
    public void detectAndSendChanges()
    {
        super.detectAndSendChanges();

        // send updates to each crafter
        for (Object crafter : crafters)
        {
            ICrafting icrafting = (ICrafting) crafter;

            // send all fields to each crafter
            for (int n = 0; n < _trackedFields.length; ++n)
            {
                int tmp = _workbench.getField(n);
                if (_trackedFields[n] != tmp)
                {
                    icrafting.sendProgressBarUpdate(this, n, tmp);
                }
            }
        }

        // cache state for next time
        for (int n = 0; n < _trackedFields.length; ++n)
        {
            _trackedFields[n] = _workbench.getField(n);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int data)
    {
        _workbench.setField(id, data);
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
        return p_94530_2_.inventory != this._craftResult && super.canMergeSlot(p_94530_1_, p_94530_2_);
    }

    private TileEntityBeaconWorkbench _workbench;
    public InventoryCrafting _craftMatrix;
    public IInventory _craftResult;
    private IInventory _craftOutput;
    private ContainerCraftingCore _craftingCore;
    private int[] _trackedFields = { 0, 0, 0 };

    private static final Logger LOGGER = LogManager.getLogger();
}
