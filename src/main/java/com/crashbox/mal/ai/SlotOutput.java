package com.crashbox.mal.ai;

import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class SlotOutput extends Slot
{
    /** The player that is using the GUI where this slot resides. */
    private final EntityPlayer thePlayer;
    private int _numOutput;

    public SlotOutput(EntityPlayer parPlayer,
                      IInventory parIInventory, int parSlotIndex,
                      int parXDisplayPosition, int parYDisplayPosition)
    {
        super(parIInventory, parSlotIndex, parXDisplayPosition,
                parYDisplayPosition);
        thePlayer = parPlayer;
    }

    @Override
    public boolean isItemValid(ItemStack stack)
    {
        return false; // can't place anything into it
    }

    @Override
    public ItemStack decrStackSize(int parAmount)
    {
        if (getHasStack())
        {
            _numOutput += Math.min(parAmount, getStack().stackSize);
        }

        return super.decrStackSize(parAmount);
    }

    @Override
    public void onPickupFromSlot(EntityPlayer playerIn, ItemStack stack)
    {
        onCrafting(stack);
        super.onPickupFromSlot(playerIn, stack);
    }

    @Override
    protected void onCrafting(ItemStack parItemStack, int parAmountGround)
    {
        _numOutput += parAmountGround;
        onCrafting(parItemStack);
    }

    @Override
    protected void onCrafting(ItemStack parItemStack)
    {
        if (!thePlayer.worldObj.isRemote)
        {
            int expEarned = _numOutput;
            float expFactor = 1.0F;

            int possibleExpEarned = MathHelper.floor_float(
                    expEarned * expFactor);

            if (possibleExpEarned < MathHelper.ceiling_float_int(
                    expEarned*expFactor)
                    && Math.random() < expEarned*expFactor-possibleExpEarned)
            {
                ++possibleExpEarned;
            }

            expEarned = possibleExpEarned;

            // create experience orbs
            int expInOrb;
            while (expEarned > 0)
            {
                expInOrb = EntityXPOrb.getXPSplit(expEarned);
                expEarned -= expInOrb;
                thePlayer.worldObj.spawnEntityInWorld(
                        new EntityXPOrb(thePlayer.worldObj, thePlayer.posX,
                                thePlayer.posY + 0.5D, thePlayer.posZ + 0.5D,
                                expInOrb));
            }
        }

        _numOutput = 0;
    }
}
