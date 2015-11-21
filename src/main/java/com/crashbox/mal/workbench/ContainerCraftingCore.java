package com.crashbox.mal.workbench;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Copyright 2015 Andrew O. Mellinger
 *
 * We provide this to inventory crafting so that we can interact with other things on demand.
 */
public class ContainerCraftingCore extends Container
{
    public ContainerCraftingCore(TileEntityAutoWorkbench workbench)
    {
        _workbench = workbench;
        _craftMatrix = _workbench.makeInventory(this);
        _world = workbench.getWorld();
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return true;
    }

    // This is all the inventory crafting uses..
    @Override
    public void onCraftMatrixChanged(IInventory inventoryIn)
    {
        this._craftResult.setInventorySlotContents(0,
                CraftingManager.getInstance().findMatchingRecipe(_craftMatrix, _world));
    }

    public InventoryCrafting getCraftingMatrix()
    {
        return _craftMatrix;
    }

    public IInventory getCraftResult()
    {
        return _craftResult;
    }

    public IInventory getCraftOutput()
    {
        return _craftOutput;
    }

    public IInventory getControls()
    {
        return _controls;
    }

    private class ControlInventory extends InventoryBasic
    {
        public ControlInventory()
        {
            super("controls", false, 1);
        }

        @Override
        public void setInventorySlotContents(int slot, ItemStack itemStack)
        {
            super.setInventorySlotContents(slot, itemStack);
            _workbench.setEnabled(itemStack != null && itemStack.stackSize > 0);
        }

        @Override
        public ItemStack decrStackSize(int i, int i1)
        {
            if (getStackInSlot(0) != null && getStackInSlot(0).stackSize == i1)
                _workbench.setEnabled(false);
            return super.decrStackSize(i, i1);
        }
    }


    private InventoryCrafting _craftMatrix;
    private IInventory _craftResult = new InventoryCraftResult();
    private IInventory _craftOutput = new InventoryBasic("output", false, 1);
    private IInventory _controls = new ControlInventory();
    private TileEntityAutoWorkbench _workbench;
    private World _world;

    private static final Logger LOGGER = LogManager.getLogger();

}
