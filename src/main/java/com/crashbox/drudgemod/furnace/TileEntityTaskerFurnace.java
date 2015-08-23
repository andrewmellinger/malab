package com.crashbox.drudgemod.furnace;

import com.crashbox.drudgemod.ai.MessageWorkerAvailability;
import com.crashbox.drudgemod.ai.TaskDeliver;
import com.crashbox.drudgemod.ai.TaskMaster;
import com.crashbox.drudgemod.messaging.Message;
import com.crashbox.drudgemod.tasker.TileEntityTaskerInventory;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
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
import net.minecraft.util.BlockPos;
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
public class TileEntityTaskerFurnace extends TileEntityTaskerInventory implements ISidedInventory
{
    public int INPUT_INDEX = 0;
    public int FUEL_INDEX = 1;
    public int OUTPUT_INDEX = 2;

    // enumerate the slots
    public enum slotEnum
    {
        INPUT_SLOT, FUEL_SLOT, OUTPUT_SLOT
    }
    private static final int[] slotsTop = new int[] { slotEnum.INPUT_SLOT.ordinal() };
    private static final int[] slotsBottom = new int[] { slotEnum.OUTPUT_SLOT.ordinal() };
    private static final int[] slotsSides = new int[] { slotEnum.FUEL_SLOT.ordinal() };

    // All the things that we contain
    private ItemStack[] _itemStacks = new ItemStack[3];

    // State trackers
    private int _remainingFuelBurnTicks;
    private int _originalFuelBurnTicks;
    private int _accumulatedItemSmeltTicks;
    private int _totalItemSmeltTicks = 200;
    private String _customName;

    public static final String NAME = "tileEntityTaskerFurnace";

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

    @Override
    public boolean shouldRefresh(World parWorld, BlockPos parPos,
                                 IBlockState parOldState, IBlockState parNewState)
    {
        return false;
    }

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
        _itemStacks[index] = stack;

        boolean isSameItemStackAlreadyInSlot = stack != null
                && stack.isItemEqual(_itemStacks[index])
                && ItemStack.areItemStackTagsEqual(stack,
                _itemStacks[index]);
        _itemStacks[index] = stack;

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
    public String getName()
    {
        return hasCustomName() ? _customName : "container.taskerFurnace";
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

    @Override
    public int getInventoryStackLimit()
    {
        return 64;
    }

    public boolean isBurning()
    {
        return _remainingFuelBurnTicks > 0;
    }


    // this function indicates whether container texture should be drawn
    @SideOnly(Side.CLIENT)
    public static boolean isBurning(IInventory inventory)
    {
        return inventory.getField(0) > 0;
    }

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
                        LOGGER.debug("update: _totalItemSmeltTicks: " + _totalItemSmeltTicks);
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
                //BlockFurnace.setState(isBurning(), worldObj, pos);
            }
        }

        if (dirtyFlag)
        {
            markDirty();
        }
    }

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
            else if (_itemStacks[2] == null)
            {
                return true;
            }
            else if (!_itemStacks[2].isItemEqual(itemstack))
            {
                return false;
            }
            else
            {
                int result = _itemStacks[2].stackSize + itemstack.stackSize;
                return result <= getInventoryStackLimit() && result <= _itemStacks[2].getMaxStackSize();
            }
        }
    }

    public void smeltItem()
    {
        if (canSmelt())
        {
            ItemStack itemstack = FurnaceRecipes.instance().getSmeltingResult(_itemStacks[0]);
            if (_itemStacks[2] == null)
            {
                _itemStacks[2] = itemstack.copy();
            }
            else if (_itemStacks[2].getItem() == itemstack.getItem())
            {
                _itemStacks[2].stackSize += itemstack.stackSize;
            }

            if (_itemStacks[0].getItem() == Item
                    .getItemFromBlock(Blocks.sponge) && _itemStacks[0]
                    .getMetadata() == 1 && _itemStacks[1] != null && _itemStacks[1].getItem() == Items.bucket)
            {
                _itemStacks[1] = new ItemStack(Items.water_bucket);
            }

            --_itemStacks[0].stackSize;
            if (_itemStacks[0].stackSize <= 0)
            {
                _itemStacks[0] = null;
            }
        }

    }


    private int getItemBurnTime(ItemStack itemStack)
    {
        return TileEntityFurnace.getItemBurnTime(itemStack);
    }


    @Override
    public boolean isUseableByPlayer(EntityPlayer playerIn)
    {
        return worldObj.getTileEntity(pos) != this ? false :
                playerIn.getDistanceSq(pos.getX()+0.5D, pos.getY()+0.5D,
                        pos.getZ()+0.5D) <= 64.0D;
    }

    @Override
    public void openInventory(EntityPlayer playerIn) {}

    @Override
    public void closeInventory(EntityPlayer playerIn) {}

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack)
    {
        if ( index == INPUT_INDEX)
        {
            if (_itemStacks[INPUT_INDEX] == null)
            {
                return FurnaceRecipes.instance().getSmeltingResult(_itemStacks[0]) != null;
            }
            if (_itemStacks[FUEL_INDEX].isItemEqual(stack))
            {
                return true;
            }
        }


        return index == slotEnum.INPUT_SLOT.ordinal() ? true : false;
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side)
    {
        return side == EnumFacing.DOWN ? slotsBottom :
                (side == EnumFacing.UP ? slotsTop : slotsSides);
    }

    @Override
    public boolean canInsertItem(int index, ItemStack itemStackIn,
                                 EnumFacing direction)
    {
        return isItemValidForSlot(index, itemStackIn);
    }

    @Override
    public boolean canExtractItem(int parSlotIndex, ItemStack parStack,
                                  EnumFacing parFacing)
    {
        return true;
    }

    @Override
    public String getGuiID()
    {
        return "drudge:taskerFurnace";
    }

    @Override
    public Container createContainer(InventoryPlayer playerInventory,
            EntityPlayer playerIn)
    {
        // DEBUG
        // Don't know when this is called.  I think the GuiHandler does all the construction
        LOGGER.error("createContainer()");
        return new ContainerTaskerFurnace(playerInventory, this);
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

    @Override
    public String toString()
    {
        return "TileEntityTasker{" +
                "_remainingFuelBurnTicks=" + _remainingFuelBurnTicks +
                ", _originalFuelBurnTicks" + _originalFuelBurnTicks +
                ", _accumulatedItemSmeltTicks=" + _accumulatedItemSmeltTicks +
                ", _totalItemSmeltTicks=" + _totalItemSmeltTicks +
                '}';
    }


    //---------------------------------------------------------------------------------------------

    private ItemStack getSmeltable()
    {
        return _itemStacks[0];
    }

    private ItemStack getFuel()
    {
        return _itemStacks[1];
    }

    private ItemStack getOutput()
    {
        return _itemStacks[2];
    }

    private int fuelNeedPriority()
    {
        return (_itemStacks[1] != null && _itemStacks[1].stackSize < 16 ? 1 : 0);
    }

    private Block getFuelBlockType()
    {
        if (_itemStacks[1] != null)
        {
            return Block.getBlockFromItem(_itemStacks[1].getItem());
        }
        return null;
    }

    private int smeltableNeedPriority()
    {
        return (_itemStacks[0] != null && _itemStacks[0].stackSize < 32 ? 1 : 0);
    }

    private ItemStack getSmeltableItemSample()
    {
        if (_itemStacks[0] != null)
        {
            return new ItemStack(_itemStacks[0].getItem(), 1, _itemStacks[0].getMetadata());
        }
        return null;
    }


    public void blockBroken()
    {
        _furnace.terminate();
    }

    //---------------------------------------------------------------------------------------------

    @Override
    public int distanceTo(BlockPos pos)
    {
        return 0;
    }

    //---------------------------------------------------------------------------------------------

    @Override
    public void setWorldObj(World worldIn)
    {
        LOGGER.debug("setWorldObj: " + worldIn);
        super.setWorldObj(worldIn);
        if (worldIn != null && !worldIn.isRemote)
        {
            _furnace = new Furnace(worldIn);
        }
        else
        {
            if (_furnace != null)
                _furnace.terminate();
            _furnace = null;
        }
    }

    // TaskMaster Furnace
    private class Furnace extends TaskMaster
    {
        Furnace(World world)
        {
            super(world);
        }

        @Override
        protected void handleMessage(Message msg)
        {
            if (msg instanceof MessageWorkerAvailability)
            {
                MessageWorkerAvailability availability = (MessageWorkerAvailability)msg;
                LOGGER.debug("Furnace " + this + " is asked for work. In progress work: " + getInProgress().size());

                int priority = smeltableNeedPriority();

                if ( priority > 0 )
                {
                    // Find smeltable
                    LOGGER.debug("Furnace can use more smeltable: " + getSmeltableItemSample().getUnlocalizedName());

                    // Indicate we need some supplies
                    availability.getAIDrudge().offer(new TaskDeliver(this, TileEntityTaskerFurnace.this,
                            getSmeltableItemSample(), INPUT_INDEX, 0));
                }
            }
        }
    }

    private Furnace _furnace;
    private static final Logger LOGGER = LogManager.getLogger();
}

