package com.crashbox.vassal.chest;

import com.crashbox.vassal.beacon.BeaconBase;
import com.crashbox.vassal.messaging.*;
import com.crashbox.vassal.beacon.TileEntityBeaconInventory;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class TileEntityBeaconChest extends TileEntityBeaconInventory implements IInventory
{
    // All the things that we contain
    private ItemStack[] _itemStacks = new ItemStack[27];

    // State trackers
    private String _customName;

    public static final String NAME = "tileEntityBeaconChest";

    private static final String NBT_ITEMS = "Items";
    private static final String NBT_SLOT = "Slot";
    private static final String NBT_CUSTOM_NAME = "CustomName";

    //=============================================================================================
    // ##### ##### #     ##### ##### #   # ##### ##### ##### #   #
    //   #     #   #     #     #     ##  #   #     #     #    # #
    //   #     #   #     ####  ####  # # #   #     #     #     #
    //   #     #   #     #     #     #  ##   #     #     #     #
    //   #   ##### ##### ##### ##### #   #   #   #####   #     #

    @Override
    public void setWorldObj(World worldIn)
    {
        LOGGER.debug("TileEntityBeaconChest setWorldObj: " + worldIn);
        super.setWorldObj(worldIn);
        if (worldIn != null && !worldIn.isRemote)
        {
            _chest = new Chest(worldIn);
        }
        else
        {
            if (_chest != null)
                _chest.terminate();
            _chest = null;
        }
    }

    //=============================================================================================
    // ##### ##### #   # #   # ##### #   # #####  ###  ####  #   #
    //   #     #   ##  # #   # #     ##  #   #   #   # #   #  # #
    //   #     #   # # #  # #  ####  # # #   #   #   # ####    #
    //   #     #   #  ##  # #  #     #  ##   #   #   # #   #   #
    // ##### ##### #   #   #   ##### #   #   #    ###  #   #   #

    @Override
    public int getSizeInventory()
    {
        return _itemStacks.length;
    }

    @Override
    public ItemStack getStackInSlot(int index)
    {
        return _itemStacks[index];
    }

    @Override
    public ItemStack decrStackSize(int index, int count)
    {
        if (_itemStacks[index] != null)
        {
            ItemStack itemstack;

            if (_itemStacks[index].stackSize <= count)
            {
                itemstack = _itemStacks[index];
                _itemStacks[index] = null;
                return itemstack;
            }
            else
            {
                itemstack = _itemStacks[index].splitStack(count);

                if (_itemStacks[index].stackSize == 0)
                {
                    _itemStacks[index] = null;
                }

                return itemstack;
            }
        }
        else
        {
            return null;
        }
    }

    /**
     * When some containers are closed they call this on each slot, then
     * drop whatever it returns as an EntityItem -
     * like when you close a workbench GUI.
     * This behavior is the same as the chest
     */
    @Override
    public ItemStack getStackInSlotOnClosing(int index)
    {
        if (_itemStacks[index] != null)
        {
            ItemStack itemstack = _itemStacks[index];
            _itemStacks[index] = null;
            return itemstack;
        }
        else
        {
            return null;
        }
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack)
    {
        _itemStacks[index] = stack;

        if (stack != null && stack.stackSize > getInventoryStackLimit())
        {
            stack.stackSize = getInventoryStackLimit();
        }
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer playerIn)
    {
        return worldObj.getTileEntity(pos) == this && playerIn
                .getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void openInventory(EntityPlayer playerIn) {}

    @Override
    public void closeInventory(EntityPlayer playerIn) {}

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack)
    {
        // All slots are valid
        if (_itemStacks[index] == null)
            return true;

        if (_itemStacks[index].stackSize >= _itemStacks[index].getMaxStackSize())
            return false;

        return _itemStacks[index].isItemEqual(stack);
    }

    @Override
    public int getField(int id)
    {
        return 0;
    }

    @Override
    public void setField(int id, int value)
    {
    }

    @Override
    public int getFieldCount()
    {
        return 0;
    }

    @Override
    public void clear()
    {
        for (int i = 0; i < _itemStacks.length; ++i)
        {
            _itemStacks[i] = null;
        }
    }

    //=============================================================================================
    // #####  ###  ##### ####  ##### ####  ##### #   # #   # ##### #   # #####  ###  ####  #   #
    //   #   #       #   #   # #     #   #   #   ##  # #   # #     ##  #   #   #   # #   #  # #
    //   #    ###    #   #   # ####  #   #   #   # # #  # #  ####  # # #   #   #   # ####    #
    //   #       #   #   #   # #     #   #   #   #  ##  # #  #     #  ##   #   #   # #   #   #
    // #####  ###  ##### ####  ##### ####  ##### #   #   #   ##### #   #   #    ###  #   #   #

    // DO WE NEED THIS FOR HOPPERS?
//    @Override
//    public int[] getSlotsForFace(EnumFacing side)
//    {
//    }
//
//    @Override
//    public boolean canInsertItem(int index, ItemStack itemStackIn,
//                                 EnumFacing direction)
//    {
//        return isItemValidForSlot(index, itemStackIn);
//    }
//
//    @Override
//    public boolean canExtractItem(int parSlotIndex, ItemStack parStack,
//                                  EnumFacing parFacing)
//    {
//        return true;
//    }


    //=============================================================================================
    // ##### #   #  ###  ####  #     ####  #   #  ###  #   # #####  ###  ####  #     #####
    //   #   #   # #   # #   # #     #   # ##  # #   # ## ## #     #   # #   # #     #
    //   #   # # # #   # ####  #     #   # # # # ##### # # # ####  ##### ##### #     ####
    //   #   ## ## #   # #   # #     #   # #  ## #   # #   # #     #   # #   # #     #
    // ##### #   #  ###  #   # ##### ####  #   # #   # #   # ##### #   # ####  ##### #####

    @Override
    public String getName()
    {
        return hasCustomName() ? _customName : "container.beaconChest";
    }

    @Override
    public boolean hasCustomName()
    {
        return _customName != null && _customName.length() > 0;
    }

    public void setCustomInventoryName(String parCustomName)
    {
        _customName = parCustomName;
    }

    //=============================================================================================
    // ##### ##### #     ##### ##### #   # ##### ##### ##### #   # #      ###   #### #   #  ###  ####  #     #####
    //   #     #   #     #     #     ##  #   #     #     #    # #  #     #   # #     #  #  #   # #   # #     #
    //   #     #   #     ####  ####  # # #   #     #     #     #   #     #   # #     ###   ##### ##### #     ####
    //   #     #   #     #     #     #  ##   #     #     #     #   #     #   # #     #  #  #   # #   # #     #
    //   #   ##### ##### ##### ##### #   #   #   #####   #     #   #####  ###   #### #   # #   # ####  ##### #####

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        NBTTagList nbttaglist = compound.getTagList(NBT_ITEMS, 10);
        _itemStacks = new ItemStack[getSizeInventory()];

        for (int i = 0; i < nbttaglist.tagCount(); ++i)
        {
            NBTTagCompound nbtTagCompound = nbttaglist.getCompoundTagAt(i);
            byte b0 = nbtTagCompound.getByte(NBT_SLOT);

            if (b0 >= 0 && b0 < _itemStacks.length)
            {
                _itemStacks[b0] = ItemStack.loadItemStackFromNBT(
                        nbtTagCompound);
            }
        }

        if (compound.hasKey(NBT_CUSTOM_NAME, 8))
        {
            _customName = compound.getString(NBT_CUSTOM_NAME);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < _itemStacks.length; ++i)
        {
            if (_itemStacks[i] != null)
            {
                NBTTagCompound nbtTagCompound = new NBTTagCompound();
                nbtTagCompound.setByte(NBT_SLOT, (byte)i);
                _itemStacks[i].writeToNBT(nbtTagCompound);
                nbttaglist.appendTag(nbtTagCompound);
            }
        }

        compound.setTag(NBT_ITEMS, nbttaglist);

        if (hasCustomName())
        {
            compound.setString(NBT_CUSTOM_NAME, _customName);
        }
    }

    //=============================================================================================
    // ##### #   # ####  ####   ###  ##### ##### ####  #      ###  #   # ##### ####  #     #####  ###  ##### ####   ###  #   #
    //   #   #   # #   # #   # #   #   #   #     #   # #     #   #  # #  #     #   # #       #   #       #   #   # #   #  # #
    //   #   #   # ####  #   # #####   #   ####  ####  #     #####   #   ####  ####  #       #    ###    #   ##### #   #   #
    //   #   #   # #     #   # #   #   #   #     #     #     #   #   #   #     #   # #       #       #   #   #   # #   #  # #
    // #####  ###  #     ####  #   #   #   ##### #     ##### #   #   #   ##### #   # ##### #####  ###    #   ####   ###  #   #

    @Override
    public void update()
    {
        if (_chest != null)
            _chest.update();
    }


    //=============================================================================================
    // ##### ##### #   # ##### ##### ####   ###   #### ##### #####  ###  #   #  ###  ####  ##### #####  #### #####
    //   #     #   ##  #   #   #     #   # #   # #       #     #   #   # ##  # #   # #   #     # #     #       #
    //   #     #   # # #   #   ####  ####  ##### #       #     #   #   # # # # #   # #####     # ####  #       #
    //   #     #   #  ##   #   #     #   # #   # #       #     #   #   # #  ## #   # #   # #   # #     #       #
    // ##### ##### #   #   #   ##### #   # #   #  ####   #   #####  ###  #   #  ###  ####   ###  #####  ####   #

    @Override
    public String getGuiID()
    {
        return "vassal:beaconChest";
    }

    @Override
    public Container createContainer(InventoryPlayer playerInventory,
            EntityPlayer playerIn)
    {
        // DEBUG
        // Don't know when this is called.  I think the GuiHandler does all the construction
        LOGGER.error("createContainer()");
        return new ContainerBeaconChest(playerInventory, this);
    }


    //=============================================================================================
    // ##### #   # #####  ###   ###   ###   #### ##### ####
    //   #   ## ## #     #     #     #   # #     #     #   #
    //   #   # # # ####   ###   ###  ##### #  ## ####  ####
    //   #   #   # #         #     # #   # #   # #     #   #
    // ##### #   # #####  ###   ###  #   #  #### ##### #   #

    @Override
    public int getRadius()
    {
        return 0;
    }

    //=============================================================================================


    @Override
    public int[] getOutputSlots()
    {
        int[] result = new int[27];
        for (int i = 0; i < 27; ++i)
            result[i] = i;
        return result;
    }

    @Override
    public int[] getInputSlots()
    {
        int[] result = new int[27];
        for (int i = 0; i < 27; ++i)
            result[i] = i;
        return result;
    }

    //=============================================================================================
    public void blockBroken()
    {
        _chest.terminate();
    }


    //---------------------------------------------------------------------------------------------
    private class Chest extends BeaconBase
    {
        Chest(World world)
        {
            super(world);
        }

        @Override
        protected IMessager getSender()
        {
            return TileEntityBeaconChest.this;
        }

        @Override
        protected int concurrentWorkerCount()
        {
            // Doesn't matter because we don't respect the concurrency count.
            return 4;
        }

        @Override
        protected void handleMessage(Message msg)
        {
            LOGGER.debug(msg);
            if (msg instanceof MessageIsStorageAvailable)
            {
                LOGGER.debug("MessageIsStorageAvailable");
                MessageIsStorageAvailable request = (MessageIsStorageAvailable)msg;

                // First look for existing
                LOGGER.debug("Looking for existing");
                for (int slotNum = 0; slotNum < _itemStacks.length; ++slotNum)
                {
                    if (_itemStacks[slotNum] != null && request.getMatcher().matches(_itemStacks[slotNum]))
                    {
//                        TRStore req = new TRStore(TileEntityBeaconChest.this, request.getSender(),
//                                msg.getTransactionID(), 0, new ItemStackMatcher(_itemStacks[slotNum]),
//                                _itemStacks[slotNum].getMaxStackSize() - _itemStacks[slotNum].stackSize, slotNum);
                        TRPutInInventory req = new TRPutInInventory(TileEntityBeaconChest.this, request.getSender(),
                                msg.getTransactionID(), 0, request.getMatcher(), 64);

                        LOGGER.debug("Posting: " + req);
                        Broadcaster.postMessage(req);
                        return;
                    }
                }

                // Do we have an empty one?
                LOGGER.debug("Looking for empty slot");
                for ( ItemStack stack : _itemStacks)
                {
                    if (stack == null)
                    {
                        TRPutInInventory req = new TRPutInInventory(TileEntityBeaconChest.this, request.getSender(),
                                msg.getTransactionID(), 0, request.getMatcher(), 64);

                        LOGGER.debug("Posting: " + req);
                        Broadcaster.postMessage(req);
                        return;
                    }
                }
            }
            else if (msg instanceof MessageItemRequest)
            {
                LOGGER.debug("Chest is getting item request: " + msg);

                MessageItemRequest request = (MessageItemRequest)msg;

                for (ItemStack stack : _itemStacks)
                {
                    if (stack != null && request.getMatcher().matches(stack))
                    {
                        TRGetFromInventory req = new TRGetFromInventory(TileEntityBeaconChest.this,
                                msg.getSender(), msg.getTransactionID(), 0, request.getMatcher(),
                                request.getQuantity());

                        LOGGER.debug("Chest advertising it has item: " + ((MessageItemRequest) msg).getMatcher());
                        Broadcaster.postMessage(req);
                        return;
                    }
                }
            }
        }
    }

    private Chest _chest;
    private static final Logger LOGGER = LogManager.getLogger();
}

