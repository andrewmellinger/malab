package com.crashbox.malab.furnace;

import com.crashbox.malab.MALabMain;
import com.crashbox.malab.ai.Priority;
import com.crashbox.malab.common.ItemStackMatcher;
import com.crashbox.malab.messaging.*;
import com.crashbox.malab.autoblock.AutoBlockBase;
import com.crashbox.malab.autoblock.TileEntityAutoBlockInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class TileEntityAutoFurnace extends TileEntityAutoBlockInventory implements ISidedInventory
{
    public int INPUT_INDEX = 0;
    public int FUEL_INDEX = 1;
    public int OUTPUT_INDEX = 2;

    public int INPUT_SAMPLE_MIN = 3;
    public int INPUT_SAMPLE_MAX = 6;
    public int FUEL_SAMPLE_MIN = 7;
    public int FUEL_SAMPLE_MAX = 10;

    // enumerate the slots
    public enum slotEnum
    {
        INPUT_SLOT, FUEL_SLOT, OUTPUT_SLOT
    }

    // We let them take buckets out of the bottom.
    private static final int[] slotsTop = new int[]{slotEnum.INPUT_SLOT.ordinal()};
    private static final int[] slotsBottom = new int[]{slotEnum.FUEL_SLOT.ordinal(), slotEnum.OUTPUT_SLOT.ordinal()};
    private static final int[] slotsSides = new int[]{slotEnum.FUEL_SLOT.ordinal()};

    // All the things that we contain
    private ItemStack[] _itemStacks = new ItemStack[11];

    // State trackers
    private int _remainingFuelBurnTicks;
    private int _originalFuelBurnTicks;
    private int _accumulatedItemSmeltTicks;
    private int _totalItemSmeltTicks = 200;
    private String _customName;

    public static final String NAME = "tileEntityAutoFurnace";

    public static final int FIELD_REMAINING_FUEL_BURN_TICKS = 0;
    public static final int FIELD_ORIGINAL_FUEL_BURN_TICKS = 1;
    public static final int FIELD_ACCUMULATED_ITEM_SMELT_TICKS = 2;
    public static final int FIELD_TOTAL_ITEM_SMELT_TICKS = 3;

    private static final String NBT_ITEMS = "Items";
    private static final String NBT_SLOT = "Slot";
    private static final String NBT_CUSTOM_NAME = "CustomName";
    private static final String NBT_REMAINING_FUEL_BURN_TICKS = "RemainingFuelBurnTime";
    private static final String NBT_ACCUMULATED_ITEM_SMELT_TICKS = "AccumulatedSmeltTime";
    private static final String NBT_TOTAL_ITEM_SMELT_TICKS = "TotalSmeltTime";

    //=============================================================================================
    // ##### ##### #     ##### ##### #   # ##### ##### ##### #   #
    //   #     #   #     #     #     ##  #   #     #     #    # #
    //   #     #   #     ####  ####  # # #   #     #     #     #
    //   #     #   #     #     #     #  ##   #     #     #     #
    //   #   ##### ##### ##### ##### #   #   #   #####   #     #

    @Override
    public void setWorldObj(World worldIn)
    {
        LOGGER.debug("setWorldObj: " + worldIn);
        super.setWorldObj(worldIn);
        if (worldIn != null && !worldIn.isRemote)
        {
            _furnace = new Furnace(worldIn);
            _broadcastHelper = new Broadcaster.BroadcastHelper(worldIn.provider.getDimensionId());
        }
        else
        {
            if (_furnace != null)
                _furnace.terminate();
            _furnace = null;
            _broadcastHelper = null;
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
     * This behavior is the same as the furnace
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
        boolean isSameItemStackAlreadyInSlot =
                stack != null &&
                        stack.isItemEqual(_itemStacks[index]) &&
                        ItemStack.areItemStackTagsEqual(stack, _itemStacks[index]);
        _itemStacks[index] = stack;

        if (_itemStacks[index] != null)
        {
            if (_itemStacks[index].stackSize == 0)
                _itemStacks[index] = null;
        }

        if (stack != null && stack.stackSize > getInventoryStackLimit())
        {
            stack.stackSize = getInventoryStackLimit();
        }

        // if input slot, reset the timers
        if (index == slotEnum.INPUT_SLOT.ordinal()
                && !isSameItemStackAlreadyInSlot)
        {
            _totalItemSmeltTicks = timeToBurnOneItem(stack);
            LOGGER.debug("setInventoryContents: _totalItemSmeltTicks: " + _totalItemSmeltTicks);
            _accumulatedItemSmeltTicks = 0;
            markDirty();
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
        return worldObj.getTileEntity(pos) == this &&
                playerIn.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D,
                        pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void openInventory(EntityPlayer playerIn)
    {
    }

    @Override
    public void closeInventory(EntityPlayer playerIn)
    {
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack)
    {
        // We don't put things in output for automation.  This is different than the furnace
        // in that we don't just take anything unless they ask for it.

        if (index == INPUT_INDEX)
        {
            if (_itemStacks[INPUT_INDEX] != null)
                return _itemStacks[INPUT_INDEX].isItemEqual(stack);
            else
                return inputSampleContains(stack);
        }
        else if (index == FUEL_INDEX)
        {
            if (_itemStacks[FUEL_INDEX] != null)
                return _itemStacks[FUEL_INDEX].isItemEqual(stack);
            else
                return fuelSampleContains(stack);
        }

        return false;
    }

    @Override
    public int getField(int id)
    {
        switch (id)
        {
            case FIELD_REMAINING_FUEL_BURN_TICKS:
                return _remainingFuelBurnTicks;
            case FIELD_ORIGINAL_FUEL_BURN_TICKS:
                return _originalFuelBurnTicks;
            case FIELD_ACCUMULATED_ITEM_SMELT_TICKS:
                return _accumulatedItemSmeltTicks;
            case FIELD_TOTAL_ITEM_SMELT_TICKS:
//                LOGGER.debug("getField: _totalItemSmeltTicks: " + _totalItemSmeltTicks);
                return _totalItemSmeltTicks;
            default:
                return 0;
        }
    }

    @Override
    public void setField(int id, int value)
    {
        switch (id)
        {
            case FIELD_REMAINING_FUEL_BURN_TICKS:
                _remainingFuelBurnTicks = value;
                break;
            case FIELD_ORIGINAL_FUEL_BURN_TICKS:
                _originalFuelBurnTicks = value;
                break;
            case FIELD_ACCUMULATED_ITEM_SMELT_TICKS:
                _accumulatedItemSmeltTicks = value;
                break;
            case FIELD_TOTAL_ITEM_SMELT_TICKS:
                _totalItemSmeltTicks = value;
//                LOGGER.debug("setField: _totalItemSmeltTicks: " + _totalItemSmeltTicks);
                break;
            default:
                break;
        }
    }

    @Override
    public int getFieldCount()
    {
        return 4;
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

    @Override
    public int[] getSlotsForFace(EnumFacing side)
    {
        return side == EnumFacing.DOWN ? slotsBottom :
                (side == EnumFacing.UP ? slotsTop : slotsSides);
    }

    @Override
    public boolean canInsertItem(int index, ItemStack stack, EnumFacing direction)
    {
        return isItemValidForSlot(index, stack);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction)
    {
        if (direction == EnumFacing.DOWN && index == 1)
        {
            Item item = stack.getItem();

            if (item != Items.water_bucket && item != Items.bucket)
            {
                return false;
            }
        }

        return true;
    }

    //=============================================================================================
    // ##### #   #  ###  ####  #     ####  #   #  ###  #   # #####  ###  ####  #     #####
    //   #   #   # #   # #   # #     #   # ##  # #   # ## ## #     #   # #   # #     #
    //   #   # # # #   # ####  #     #   # # # # ##### # # # ####  ##### ##### #     ####
    //   #   ## ## #   # #   # #     #   # #  ## #   # #   # #     #   # #   # #     #
    // ##### #   #  ###  #   # ##### ####  #   # #   # #   # ##### #   # ####  ##### #####

    @Override
    public String getName()
    {
        return hasCustomName() ? _customName : "container.malab.furnace";
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

        _remainingFuelBurnTicks = compound.getShort(NBT_REMAINING_FUEL_BURN_TICKS);
        _accumulatedItemSmeltTicks = compound.getShort(NBT_ACCUMULATED_ITEM_SMELT_TICKS);
        _totalItemSmeltTicks = compound.getShort(NBT_TOTAL_ITEM_SMELT_TICKS);
        LOGGER.debug("readFromNBT: _totalItemSmeltTicks: " + _totalItemSmeltTicks);

        if (compound.hasKey(NBT_CUSTOM_NAME, 8))
        {
            _customName = compound.getString(NBT_CUSTOM_NAME);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        compound.setShort(NBT_REMAINING_FUEL_BURN_TICKS, (short) _remainingFuelBurnTicks);
        compound.setShort(NBT_ACCUMULATED_ITEM_SMELT_TICKS, (short) _accumulatedItemSmeltTicks);
        compound.setShort(NBT_TOTAL_ITEM_SMELT_TICKS, (short) _totalItemSmeltTicks);
        NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < _itemStacks.length; ++i)
        {
            if (_itemStacks[i] != null)
            {
                NBTTagCompound nbtTagCompound = new NBTTagCompound();
                nbtTagCompound.setByte(NBT_SLOT, (byte) i);
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

    // this function indicates whether container texture should be drawn
    @SideOnly(Side.CLIENT)
    public static boolean isBurning(IInventory inventory)
    {
        return inventory.getField(0) > 0;
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
        // Refactor
        boolean isBurningFlag = isBurning();
        boolean dirtyFlag = false;

        if (isBurning())
        {
            --_remainingFuelBurnTicks;
        }

        if (!worldObj.isRemote)
        {
            if (_furnace != null)
                _furnace.update();


            // what does this do?
            if (!isBurning() && (_itemStacks[1] == null || _itemStacks[0] == null))
            {
                if (!isBurning() && _accumulatedItemSmeltTicks > 0)
                {
                    _accumulatedItemSmeltTicks = MathHelper.clamp_int(_accumulatedItemSmeltTicks - 2, 0, _totalItemSmeltTicks);
                }
            }
            else
            {
                // if we're not burning but we can smelt something, started it up
                if (!isBurning() && canSmelt())
                {
                    // NOTE: isBurning() is based off of _remainingFuelBurnTicks
                    _originalFuelBurnTicks = _remainingFuelBurnTicks = getItemBurnTime(_itemStacks[1]);

                    // since we can burn something decrement the item count
                    if (isBurning())
                    {
                        dirtyFlag = true;
                        if (_itemStacks[1] != null)
                        {
                            --_itemStacks[1].stackSize;
                            if (_itemStacks[1].stackSize == 0)
                            {
                                _itemStacks[1] = _itemStacks[1].getItem().getContainerItem(_itemStacks[1]);
                            }
                        }
                    }
                }

                // Now we need to move along the burning process
                if (isBurning() && canSmelt())
                {
                    ++_accumulatedItemSmeltTicks;
                    if (_accumulatedItemSmeltTicks == _totalItemSmeltTicks)
                    {
                        _accumulatedItemSmeltTicks = 0;
                        _totalItemSmeltTicks = timeToBurnOneItem(_itemStacks[0]);
                        //LOGGER.debug("isStillBreaking: _totalItemSmeltTicks: " + _totalItemSmeltTicks);
                        smeltItem();
                        dirtyFlag = true;
                    }
                }
                else
                {
                    _accumulatedItemSmeltTicks = 0;
                }
            }

            if (isBurningFlag != isBurning())
            {
                dirtyFlag = true;
                BlockAutoFurnace.setState(isBurning(), worldObj, pos);
            }
        }

        if (dirtyFlag)
        {
            markDirty();
        }
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
        return MALabMain.MODID + ":furnace";
    }

    @Override
    public Container createContainer(InventoryPlayer playerInventory,
                                     EntityPlayer playerIn)
    {
        // DEBUG
        // Don't know when this is called.  I think the GuiHandler does all the construction
        LOGGER.error("createContainer()");
        return new ContainerAutoFurnace(playerInventory, this);
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
    // ##### ##### #     ##### ##### #   # ##### ##### ##### #   # ####  #####  ###   ####  ###  #   # ##### #   # #   # ##### #   # #####  ###  ####  #   #
    //   #     #   #     #     #     ##  #   #     #     #    # #  #   # #     #   # #     #   # ##  #   #   ##  # #   # #     ##  #   #   #   # #   #  # #
    //   #     #   #     ####  ####  # # #   #     #     #     #   ##### ####  ##### #     #   # # # #   #   # # #  # #  ####  # # #   #   #   # ####    #
    //   #     #   #     #     #     #  ##   #     #     #     #   #   # #     #   # #     #   # #  ##   #   #  ##  # #  #     #  ##   #   #   # #   #   #
    //   #   ##### ##### ##### ##### #   #   #   #####   #     #   ####  ##### #   #  ####  ###  #   # ##### #   #   #   ##### #   #   #    ###  #   #   #

    @Override
    public int[] getOutputSlots()
    {
        return new int[]{OUTPUT_INDEX};
    }

    @Override
    public int[] getInputSlots()
    {
        return new int[]{INPUT_INDEX, FUEL_INDEX};
    }


    //=============================================================================================

    public boolean isBurning()
    {
        return _remainingFuelBurnTicks > 0;
    }

    @SuppressWarnings("UnusedParameters")
    public int timeToBurnOneItem(ItemStack itemStack)
    {
        return 200;
    }

    private boolean canSmelt()
    {
        if (_itemStacks[0] == null)
        {
            return false;
        }
        else
        {
            ItemStack itemstack = FurnaceRecipes.instance().getSmeltingResult(_itemStacks[0]);
            if (itemstack == null)
            {
                return false;
            }
            else if (_itemStacks[OUTPUT_INDEX] == null)
            {
                return true;
            }
            else if (!_itemStacks[OUTPUT_INDEX].isItemEqual(itemstack))
            {
                return false;
            }
            else
            {
                int result = _itemStacks[OUTPUT_INDEX].stackSize + itemstack.stackSize;
                return result <= getInventoryStackLimit() && result <= _itemStacks[OUTPUT_INDEX].getMaxStackSize();
            }
        }
    }

    public void smeltItem()
    {
        if (canSmelt())
        {
            ItemStack itemstack = FurnaceRecipes.instance().getSmeltingResult(_itemStacks[INPUT_INDEX]);

            // Fuel slot to 8, output slot to almost full, fuel slot, then output slot

            // Automatically refuel if we take the same item
            if (_itemStacks[FUEL_INDEX] != null &&
                    _itemStacks[FUEL_INDEX].stackSize < 4 &&
                    _itemStacks[FUEL_INDEX].isItemEqual(itemstack))
            {
                // In case we have multiple outputs
                _itemStacks[FUEL_INDEX].stackSize += itemstack.stackSize;
            }
            else if (_itemStacks[FUEL_INDEX] == null && fuelSampleContains(itemstack))
            {
                _itemStacks[FUEL_INDEX] = itemstack.copy();
            }
            else if (_itemStacks[OUTPUT_INDEX] == null)
            {
                _itemStacks[OUTPUT_INDEX] = itemstack.copy();
            }
            else if (_itemStacks[OUTPUT_INDEX].getItem() == itemstack.getItem() &&
                    _itemStacks[OUTPUT_INDEX].stackSize <
                            (_itemStacks[OUTPUT_INDEX].getMaxStackSize() - itemstack.stackSize - 4))
            {
                _itemStacks[OUTPUT_INDEX].stackSize += itemstack.stackSize;
            }
            else if (_itemStacks[FUEL_INDEX] != null &&
                    _itemStacks[FUEL_INDEX].stackSize < 60 &&
                    _itemStacks[FUEL_INDEX].isItemEqual(itemstack))
            {
                // Keep working on fuel.
                _itemStacks[FUEL_INDEX].stackSize += itemstack.stackSize;
            }
            else if (_itemStacks[OUTPUT_INDEX].getItem() == itemstack.getItem() &&
                    _itemStacks[OUTPUT_INDEX].stackSize <
                            (_itemStacks[OUTPUT_INDEX].getMaxStackSize() - itemstack.stackSize))
            {
                // Now finish up with the output slot
                _itemStacks[OUTPUT_INDEX].stackSize += itemstack.stackSize;
            }

            // TODO:  What does this mean??  What does sponge have to with lava?
            if (_itemStacks[INPUT_INDEX].getItem() == Item.getItemFromBlock(Blocks.sponge) &&
                    _itemStacks[INPUT_INDEX].getMetadata() == 1 &&
                    _itemStacks[FUEL_INDEX] != null &&
                    _itemStacks[FUEL_INDEX].getItem() == Items.bucket)
            {
                _itemStacks[FUEL_INDEX] = new ItemStack(Items.water_bucket);
            }

            --_itemStacks[INPUT_INDEX].stackSize;
            if (_itemStacks[INPUT_INDEX].stackSize <= 0)
            {
                _itemStacks[INPUT_INDEX] = null;
            }
        }
    }

    private int getItemBurnTime(ItemStack itemStack)
    {
        return TileEntityFurnace.getItemBurnTime(itemStack);
    }

    //---------------------------------------------------------------------------------------------

    private int smeltableNeedPriority()
    {
        if (_itemStacks[INPUT_INDEX] == null)
        {
            return 100;
        }

        // We have a priority based on space
        int size = _itemStacks[INPUT_INDEX].stackSize;
        int maxSize = _itemStacks[INPUT_INDEX].getMaxStackSize();
        int space = _itemStacks[INPUT_INDEX].getMaxStackSize() - size;

        float MAX_REQUEST_THRESHOLD = 0.75F;
        if (size > (MAX_REQUEST_THRESHOLD * maxSize))
            return 0;

        return (space * 50) / maxSize;
    }

    private boolean inputSampleContains(ItemStack stack)
    {
        for (int i = INPUT_SAMPLE_MIN; i <= INPUT_SAMPLE_MAX; ++i)
        {
            if (_itemStacks[i] != null && _itemStacks[i].isItemEqual(stack))
                return true;
        }
        return false;
    }

    private ItemStackMatcher getSmeltableItemMatcher()
    {
        if (_itemStacks[0] != null)
        {
            return new ItemStackMatcher(_itemStacks[0]);
        }

        return buildSampleMatcher(INPUT_SAMPLE_MIN, INPUT_SAMPLE_MAX);
    }

    /**
     * @return How much smeltable material we can consume.
     */
    private int getSmeltableQuantityWanted()
    {
        if (_itemStacks[INPUT_INDEX] == null)
            return 64;

        // We'll take all the free space.
        return _itemStacks[INPUT_INDEX].getMaxStackSize() - _itemStacks[INPUT_INDEX].stackSize;
    }

    //---------------------------

    private ItemStack getFuel()
    {
        return _itemStacks[FUEL_INDEX];
    }

    private boolean fuelSampleContains(ItemStack stack)
    {
        for (int i = FUEL_SAMPLE_MIN; i <= FUEL_SAMPLE_MAX; ++i)
        {
            if (_itemStacks[i] != null && _itemStacks[i].isItemEqual(stack))
                return true;
        }
        return false;
    }

    private ItemStackMatcher getFuelSamples()
    {
        // If we have some then just that.
        ItemStack fuelStack = getFuel();
        if (fuelStack != null)
            return new ItemStackMatcher(fuelStack);

        return buildSampleMatcher(FUEL_SAMPLE_MIN, FUEL_SAMPLE_MAX);
    }

    private int getFuelQuantityWanted()
    {
        if (getFuel() == null)
            return 64;

        return getFuel().getMaxStackSize() - getFuel().stackSize;
    }


    //---------------------------

    public void blockBroken()
    {
        _furnace.terminate();
    }

    //---------------------------------------------------------------------------------------------
    private ItemStackMatcher buildSampleMatcher(int min, int max)
    {
        // If we aren't consuming anything, then bring any of the samples
        ItemStackMatcher matcher = new ItemStackMatcher();
        for (int i = min; i <= max; ++i)
        {
            if (_itemStacks[i] != null)
            {
                matcher.add(_itemStacks[i]);
            }
        }

        if (matcher.size() == 0)
            return null;

        return matcher;
    }

    //---------------------------------------------------------------------------------------------
    private class Furnace extends AutoBlockBase
    {
        Furnace(World world)
        {
            super(world);
        }

        @Override
        protected IMessager getSender()
        {
            return TileEntityAutoFurnace.this;
        }

        @Override
        protected int concurrentWorkerCount()
        {
            // We can handle quite a few because they don't take long here.
            return 2;
        }

        @Override
        protected void handleMessage(Message msg)
        {
            if (msg instanceof MessageWorkerAvailability)
            {
                // We don't want to ask everyone to collect resources for us all the time
                if (haveFreeWorkerSlots(msg))
                    handleWorkerAvailability((MessageWorkerAvailability) msg);
            }
            else if (msg instanceof MessageIsStorageAvailable)
            {
                MessageIsStorageAvailable request = (MessageIsStorageAvailable) msg;

                checkInputAccepts(request, INPUT_INDEX, INPUT_SAMPLE_MIN, INPUT_SAMPLE_MAX);
                checkInputAccepts(request, FUEL_INDEX, FUEL_SAMPLE_MIN, FUEL_SAMPLE_MAX);
            }
            else if (msg instanceof MessageItemRequest)
            {
                MessageItemRequest req = (MessageItemRequest) msg;
                if (_itemStacks[OUTPUT_INDEX] != null && req.getMatcher().matches(_itemStacks[OUTPUT_INDEX]))
                {
                    int qty = req.getQuantity();
                    if (qty > _itemStacks[OUTPUT_INDEX].stackSize)
                        qty = _itemStacks[OUTPUT_INDEX].stackSize;

                    TRGetFromInventory newReq = new TRGetFromInventory(TileEntityAutoFurnace.this,
                            msg.getSender(), msg.getTransactionID(), 0, req.getMatcher(), qty);
                    _broadcastHelper.postMessage(newReq);
                }
            }
        }
    }

    private void handleWorkerAvailability(MessageWorkerAvailability msg)
    {
        int value = smeltableNeedPriority();
        if (value > 0)
        {
            // Find smeltable
            ItemStackMatcher matcher = getSmeltableItemMatcher();
            if (matcher != null)
            {
                // Send a message back to this guy telling him that we could use more
                TRPutInInventory req = new TRPutInInventory(TileEntityAutoFurnace.this,
                        msg.getSender(), msg.getTransactionID(), value, getSmeltableItemMatcher(),
                        getSmeltableQuantityWanted());

                LOGGER.debug("Furnace= " + getPos() + ", posting=" + req);
                _broadcastHelper.postMessage(req);
            }
        }

        value = Priority.getFuelNeed(getFuel());
        if (value > 0)
        {
            // TODO:  Compute quantity needed.
            ItemStackMatcher matcher = getFuelSamples();
            if (matcher != null)
            {
                TRPutInInventory req = new TRPutInInventory(TileEntityAutoFurnace.this,
                        msg.getSender(), msg.getTransactionID(),
                        value, matcher, getFuelQuantityWanted());
                _broadcastHelper.postMessage(req);
            }
        }
    }


    private void checkInputAccepts(MessageIsStorageAvailable msg, int slot, int sampleMin, int sampleMax)
    {
        ItemStack stack = _itemStacks[slot];
        if (stack != null)
        {
            if (msg.getMatcher().matches(stack))
            {
                int free = stack.getMaxStackSize() - stack.stackSize;
                if (free > 8)
                {
                    // Send a message back to this guy telling him that we could use more
                    TRPutInInventory req = new TRPutInInventory(TileEntityAutoFurnace.this,
                            msg.getSender(), msg.getTransactionID(), 0, msg.getMatcher(),
                            free);

                    _broadcastHelper.postMessage(req);
                }
            }
        }
        else
        {
            // Check samples
            for (int i = sampleMin; i <= sampleMax; ++i)
            {
                if (_itemStacks[i] != null && msg.getMatcher().matches(_itemStacks[i]))
                {
                    int free = _itemStacks[i].getMaxStackSize() - _itemStacks[i].stackSize;
                    if (free > 8)
                    {
                        TRPutInInventory req = new TRPutInInventory(TileEntityAutoFurnace.this,
                                msg.getSender(), msg.getTransactionID(), 0, msg.getMatcher(),
                                free);

                        _broadcastHelper.postMessage(req);
                        break;
                    }
                }
            }
        }
    }

    //---------------------------------------------------------------------------------------------

    @Override
    public String toString()
    {
        return "TEBFurnace@" + Integer.toHexString(this.hashCode()) + "{" +
                "remainBurnTicks=" + _remainingFuelBurnTicks +
                ", origBurnTicks" + _originalFuelBurnTicks +
                ", accSmeltTicks=" + _accumulatedItemSmeltTicks +
                ", totalSmeltTicks=" + _totalItemSmeltTicks +
                '}';
    }

    private Furnace _furnace;
    private Broadcaster.BroadcastHelper _broadcastHelper;

    private static final Logger LOGGER = LogManager.getLogger();
}

