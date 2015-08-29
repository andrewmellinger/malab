package com.crashbox.drudgemod.workbench;

import com.crashbox.drudgemod.DrudgeMain;
import com.crashbox.drudgemod.ai.MessageWorkerAvailability;
import com.crashbox.drudgemod.ai.TaskDeliver;
import com.crashbox.drudgemod.ai.TaskMaster;
import com.crashbox.drudgemod.messaging.Message;
import com.crashbox.drudgemod.tasker.TileEntityTaskerInventory;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityLockable;
import net.minecraft.util.*;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.IWorldNameable;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class TileEntityTaskerWorkbench extends TileEntity implements IUpdatePlayerListBox, IInteractionObject
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
    public ItemStack[] _itemStacks = new ItemStack[10];

    // State trackers
    public int _remainingFuelBurnTicks;
    public int _originalFuelBurnTicks;
    public int _accumulatedItemSmeltTicks;
    public int _totalItemSmeltTicks = 200;
    private String _customName;

    public static final String NAME = "tileEntityTaskerWorkbench";

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


    public TileEntityTaskerWorkbench()
    {

    }

    @Override
    public boolean shouldRefresh(World parWorld, BlockPos parPos,
                                 IBlockState parOldState, IBlockState parNewState)
    {
        return false;
    }

    //=============================================================================================
    // Tile Entity
    //=============================================================================================
    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        NBTTagList nbttaglist = compound.getTagList(NBT_ITEMS, 10);
//        _itemStacks = new ItemStack[getSizeInventory()];
        _itemStacks = new ItemStack[10];

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
    public void setWorldObj(World worldIn)
    {
        LOGGER.debug("setWorldObj: " + worldIn);
        super.setWorldObj(worldIn);
//        if (worldIn != null && !worldIn.isRemote)
//        {
//            _workbench = new Workbench(worldIn);
//        }
//        else
//        {
//            if (_workbench != null)
//                _workbench.terminate();
//            _workbench = null;
//        }
    }


    //=============================================================================================
    // TileEntityLockable
    //=============================================================================================

    public InventoryTaskerWorkbench makeInventory(ContainerTaskerWorkbench container)
    {
        return new InventoryTaskerWorkbench(container);
    }

    private class InventoryTaskerWorkbench extends InventoryCrafting
    {
        private InventoryTaskerWorkbench(Container container)
        {
            super(container, 3, 3);
        }

        // TODO:  Add get field/set field to make this work right.
    }


    //=============================================================================================
    // Inventory
    //=============================================================================================

//
//    @Override
//    public int getSizeInventory()
//    {
//        return _itemStacks.length;
//    }
//
//    @Override
//    public ItemStack getStackInSlot(int index)
//    {
//        return _itemStacks[index];
//    }
//
//    @Override
//    public ItemStack decrStackSize(int index, int count)
//    {
//        if (_itemStacks[index] != null)
//        {
//            ItemStack itemstack;
//
//            if (_itemStacks[index].stackSize <= count)
//            {
//                itemstack = _itemStacks[index];
//                _itemStacks[index] = null;
//                return itemstack;
//            }
//            else
//            {
//                itemstack = _itemStacks[index].splitStack(count);
//
//                if (_itemStacks[index].stackSize == 0)
//                {
//                    _itemStacks[index] = null;
//                }
//
//                return itemstack;
//            }
//        }
//        else
//        {
//            return null;
//        }
//    }
//
//    /**
//     * When some containers are closed they call this on each slot, then
//     * drop whatever it returns as an EntityItem -
//     * like when you close a workbench GUI.
//     * This behavior is the same as the furnace
//     */
//    @Override
//    public ItemStack getStackInSlotOnClosing(int index)
//    {
//        if (_itemStacks[index] != null)
//        {
//            ItemStack itemstack = _itemStacks[index];
//            _itemStacks[index] = null;
//            return itemstack;
//        }
//        else
//        {
//            return null;
//        }
//    }
//
//    @Override
//    public void setInventorySlotContents(int index, ItemStack stack)
//    {
//        _itemStacks[index] = stack;
//
//        boolean isSameItemStackAlreadyInSlot = stack != null
//                && stack.isItemEqual(_itemStacks[index])
//                && ItemStack.areItemStackTagsEqual(stack,
//                _itemStacks[index]);
//        _itemStacks[index] = stack;
//
//        if (stack != null && stack.stackSize > getInventoryStackLimit())
//        {
//            stack.stackSize = getInventoryStackLimit();
//        }
//
//        // if input slot, reset the timers
//        if (index == slotEnum.INPUT_SLOT.ordinal()
//                && !isSameItemStackAlreadyInSlot)
//        {
//            _totalItemSmeltTicks = timeToBurnOneItem(stack);
//            LOGGER.debug("setInventoryContents: _totalItemSmeltTicks: " + _totalItemSmeltTicks);
//            _accumulatedItemSmeltTicks = 0;
//            markDirty();
//        }
//    }
//
//    @Override
//    public int getInventoryStackLimit()
//    {
//        return 64;
//    }
//
//    @Override
//    public boolean isUseableByPlayer(EntityPlayer playerIn)
//    {
//        return worldObj.getTileEntity(pos) != this ? false :
//                playerIn.getDistanceSq(pos.getX()+0.5D, pos.getY()+0.5D,
//                        pos.getZ()+0.5D) <= 64.0D;
//    }
//
//    @Override
//    public void openInventory(EntityPlayer playerIn) {}
//
//    @Override
//    public void closeInventory(EntityPlayer playerIn) {}
//
//    @Override
//    public boolean isItemValidForSlot(int index, ItemStack stack)
//    {
//        if ( index == INPUT_INDEX)
//        {
//            if (_itemStacks[INPUT_INDEX] == null)
//            {
//                return FurnaceRecipes.instance().getSmeltingResult(_itemStacks[0]) != null;
//            }
//            if (_itemStacks[FUEL_INDEX].isItemEqual(stack))
//            {
//                return true;
//            }
//        }
//
//
//        return index == slotEnum.INPUT_SLOT.ordinal() ? true : false;
//    }
//
//    @Override
//    public int getField(int id)
//    {
//        switch (id)
//        {
//            case FIELD_REMAINING_FUEL_BURN_TICKS:
//                return _remainingFuelBurnTicks;
//            case FIELD_ORIGINAL_FUEL_BURN_TICKS:
//                return _originalFuelBurnTicks;
//            case FIELD_ACCUMULATED_ITEM_SMELT_TICKS:
//                return _accumulatedItemSmeltTicks;
//            case FIELD_TOTAL_ITEM_SMELT_TICKS:
////                LOGGER.debug("getField: _totalItemSmeltTicks: " + _totalItemSmeltTicks);
//                return _totalItemSmeltTicks;
//            default:
//                return 0;
//        }
//    }
//
//    @Override
//    public void setField(int id, int value)
//    {
//        switch (id)
//        {
//            case FIELD_REMAINING_FUEL_BURN_TICKS:
//                _remainingFuelBurnTicks = value;
//                break;
//            case FIELD_ORIGINAL_FUEL_BURN_TICKS:
//                _originalFuelBurnTicks = value;
//                break;
//            case FIELD_ACCUMULATED_ITEM_SMELT_TICKS:
//                _accumulatedItemSmeltTicks = value;
//                break;
//            case FIELD_TOTAL_ITEM_SMELT_TICKS:
//                _totalItemSmeltTicks = value;
////                LOGGER.debug("setField: _totalItemSmeltTicks: " + _totalItemSmeltTicks);
//                break;
//            default:
//                break;
//        }
//    }
//
//    @Override
//    public int getFieldCount()
//    {
//        return 4;
//    }
//
//    @Override
//    public void clear()
//    {
//        for (int i = 0; i < _itemStacks.length; ++i)
//        {
//            _itemStacks[i] = null;
//        }
//    }

    //=============================================================================================
    // ISidedInventory
    //=============================================================================================

//    @Override
//    public int[] getSlotsForFace(EnumFacing side)
//    {
//        return side == EnumFacing.DOWN ? slotsBottom :
//                (side == EnumFacing.UP ? slotsTop : slotsSides);
//    }
//
//    @Override
//    public boolean canInsertItem(int index, ItemStack itemStackIn,
//            EnumFacing direction)
//    {
//        return isItemValidForSlot(index, itemStackIn);
//    }
//
//    @Override
//    public boolean canExtractItem(int parSlotIndex, ItemStack parStack,
//            EnumFacing parFacing)
//    {
//        return true;
//    }


    //=============================================================================================
    // IWorldNameable
    //=============================================================================================

    // We can redirect to the
    @Override
    public String getName()
    {
        return hasCustomName() ? _customName : "container.taskerWorkbench";
    }

    @Override
    public boolean hasCustomName()
    {
        return _customName != null && _customName.length() > 0;
    }

    public IChatComponent getDisplayName()
    {
        return new ChatComponentTranslation("container.taskerWorkbench");
    }

    //=============================================================================================
    // IInteractionObject
    //=============================================================================================

    @Override
    public String getGuiID()
    {
        return "drudge:taskerWorkbench";
    }

    @Override
    public Container createContainer(InventoryPlayer playerInventory,
            EntityPlayer playerIn)
    {
        // DEBUG
        // Don't know when this is called.  I think the GuiHandler does all the construction
        LOGGER.error("createContainer()");
        return new ContainerWorkbench(playerInventory, getWorld(), getPos());
    }

    //=============================================================================================
    // IUpdatePlayerListBox
    //=============================================================================================

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
                //BlockWorkbench.setState(isBurning(), worldObj, pos);
            }
        }

        if (dirtyFlag)
        {
            markDirty();
        }
    }



    //=============================================================================================
    // Custom
    //=============================================================================================

    public boolean isBurning()
    {
        return _remainingFuelBurnTicks > 0;
    }

    // this function indicates whether container texture should be drawn
    @SideOnly(Side.CLIENT)
    public static boolean isBurning(TileEntityTaskerWorkbench workbench)
    {
        return workbench._remainingFuelBurnTicks > 0;
    }

    public int timeToBurnOneItem(ItemStack itemStack)
    {
        return 200;
    }

    private boolean canSmelt()
    {
        if (_itemStacks[INPUT_INDEX] == null)
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
//                return result <= getInventoryStackLimit() && result <= _itemStacks[2].getMaxStackSize();
                return result <= 64 && result <= _itemStacks[OUTPUT_INDEX].getMaxStackSize();
            }
        }
    }

    public void smeltItem()
    {
        if (canSmelt())
        {
            ItemStack itemstack = FurnaceRecipes.instance().getSmeltingResult(_itemStacks[INPUT_INDEX]);

            // Automatically refuel.  Bascially, this is form charcoal
            if (_itemStacks[FUEL_INDEX] != null &&
                    _itemStacks[FUEL_INDEX].stackSize < 48 &&
                    _itemStacks[FUEL_INDEX].isItemEqual(itemstack))
            {
                // In case we have multiple outputs
                _itemStacks[FUEL_INDEX].stackSize += itemstack.stackSize;
            }
            else if (_itemStacks[OUTPUT_INDEX] == null)
            {
                _itemStacks[OUTPUT_INDEX] = itemstack.copy();
            }
            else if (_itemStacks[OUTPUT_INDEX].getItem() == itemstack.getItem())
            {
                _itemStacks[OUTPUT_INDEX].stackSize += itemstack.stackSize;
            }

            // TODO:  What does this mean??
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

    @Deprecated
    private int getItemBurnTime(ItemStack itemStack)
    {
        return 0;
    }


    private ItemStack getSmeltable()
    {
        return _itemStacks[INPUT_INDEX];
    }

    private int smeltableNeedPriority()
    {
        if (_itemStacks[INPUT_INDEX] == null)
        {
            // We don't know what to ask for.
            return 0;
        }

        int size = _itemStacks[INPUT_INDEX].stackSize;
        if ( size < (_itemStacks[INPUT_INDEX].getMaxStackSize() * _smeltableRequestFraction))
        {
            return 1;
        }
        return 0;
    }

    private ItemStack getSmeltableItemSample()
    {
        if (_itemStacks[0] != null)
        {
            return new ItemStack(_itemStacks[INPUT_INDEX].getItem(), 1, _itemStacks[INPUT_INDEX].getMetadata());
        }
        return null;
    }

    /**
     * @return How much smeltable material we can consume.
     */
    private int getSmeltableQuantityWanted()
    {
        // We'll take all the free space.
        return _itemStacks[INPUT_INDEX].getMaxStackSize() - _itemStacks[INPUT_INDEX].stackSize;
    }

    //---------------------------

    private ItemStack getFuel()
    {
        return _itemStacks[FUEL_INDEX];
    }

    //---------------------------

    private ItemStack getOutput()
    {
        return _itemStacks[OUTPUT_INDEX];
    }

    public void blockBroken()
    {
        //_workbench.terminate();
    }

    //---------------------------------------------------------------------------------------------

//    @Override
//    public int distanceTo(BlockPos pos)
//    {
//        return 0;
//    }

    //---------------------------------------------------------------------------------------------

    // TaskMaster Workbench
//    private class Workbench extends TaskMaster
//    {
//        Workbench(World world)
//        {
//            super(world);
//        }
//
//        @Override
//        protected void handleMessage(Message msg)
//        {
//            if (msg instanceof MessageWorkerAvailability)
//            {
//                MessageWorkerAvailability availability = (MessageWorkerAvailability)msg;
//                LOGGER.debug("Workbench " + this + " is asked for work. In progress work: " + getInProgress().size());
//
//                int priority = smeltableNeedPriority();
//
//                if ( priority > 0 )
//                {
//                    // Find smeltable
//                    LOGGER.debug("Workbench can use more smeltable: " + getSmeltableItemSample().getUnlocalizedName());
//
//                    // Indicate we need some supplies
//                    availability.getAIDrudge().offer(new TaskDeliver(this, TileEntityTaskerWorkbench.this,
//                            getSmeltableItemSample(), INPUT_INDEX, getSmeltableQuantityWanted()));
//                }
//            }
//        }
//    }


    @Override
    public String toString()
    {
        return "TileEntityTaskerWorkbench{" +
                "_remainingFuelBurnTicks=" + _remainingFuelBurnTicks +
                ", _originalFuelBurnTicks" + _originalFuelBurnTicks +
                ", _accumulatedItemSmeltTicks=" + _accumulatedItemSmeltTicks +
                ", _totalItemSmeltTicks=" + _totalItemSmeltTicks +
                '}';
    }

    //---------------------------------------------------------------------------------------------

    private float _fuelRequestFraction = 0.5F;
    private float _smeltableRequestFraction = 0.5F;

//    private InventoryCrafting _craftingInventory =  new InventoryCrafting(this, 3, 3);

    //private Workbench _workbench;
    private static final Logger LOGGER = LogManager.getLogger();
}

