package com.crashbox.vassal.workbench;

import com.crashbox.vassal.util.VassalUtils;
import com.crashbox.vassal.ai.Priority;
import com.crashbox.vassal.beacon.BeaconBase;
import com.crashbox.vassal.beacon.TileEntityBeaconInventory;
import com.crashbox.vassal.common.ItemStackMatcher;
import com.crashbox.vassal.messaging.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;


/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class TileEntityBeaconWorkbench extends TileEntityBeaconInventory implements ISidedInventory
{
    public static final String NAME = "tileEntityBeaconWorkbench";

    public TileEntityBeaconWorkbench()
    {
        // This is the tricky part.  We create a persistent inventory so we can interact with it
        // when closed.  We want to use the normal InventoryCrafting but it takes a container
        // callback so we have to provide a proxy to get it to redirect.
        _craftingCore = new ContainerCraftingCore(this);
        _craftingMatrix = _craftingCore.getCraftingMatrix();
        _craftResult = _craftingCore.getCraftResult();
        _craftOutput = _craftingCore.getCraftOutput();
        _controls = _craftingCore.getControls();
    }

    //=============================================================================================
    // ##### ##### #     ##### ##### #   # ##### ##### ##### #   #
    //   #     #   #     #     #     ##  #   #     #     #    # #
    //   #     #   #     ####  ####  # # #   #     #     #     #
    //   #     #   #     #     #     #  ##   #     #     #     #
    //   #   ##### ##### ##### ##### #   #   #   #####   #     #

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        LOGGER.debug("readFromNBT: " + compound);
        super.readFromNBT(compound);
        // NOTE:  10 is a type not a size...
        NBTTagList nbttaglist = compound.getTagList(NBT_ITEMS, 10);
//        LOGGER.debug("after super read: " + compound);
//        LOGGER.debug("Item="+nbttaglist);

        boolean enabled = false;
        for (int i = 0; i < nbttaglist.tagCount(); ++i)
        {
            NBTTagCompound nbtTagCompound = nbttaglist.getCompoundTagAt(i);
            byte b0 = nbtTagCompound.getByte(NBT_SLOT);

            // This is the output slot
            if (b0 == 9)
            {
                ItemStack stack = ItemStack.loadItemStackFromNBT(nbtTagCompound);
                _craftOutput.setInventorySlotContents(0, stack);
//                LOGGER.debug("craftedOutput from NBT: slot=" + b0 +", stack=" + stack);
            }
            // Control slot
            else if (b0 == 10)
            {
                ItemStack stack = ItemStack.loadItemStackFromNBT(nbtTagCompound);
                _controls.setInventorySlotContents(0, stack);
                enabled = (stack != null && stack.stackSize > 0);
//                LOGGER.debug("control from NBT: slot=" + b0 +", stack=" + stack);
            }
            else if (b0 >= 0 && b0 < _craftingMatrix.getSizeInventory())
            {
                ItemStack stack = ItemStack.loadItemStackFromNBT(nbtTagCompound);
                _craftingMatrix.setInventorySlotContents(b0, stack);
//                LOGGER.debug("itemStack from NBT: slot=" + b0 +", stack=" + stack);
            }
        }

        _ticksToCraft = compound.getShort(NBT_TICKS_TO_CRAFT);
        _ticksCrafted = compound.getShort(NBT_TICKS_CRAFTED);
        setEnabled(enabled);

        if (compound.hasKey(NBT_CUSTOM_NAME, 8))
        {
            _customName = compound.getString(NBT_CUSTOM_NAME);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
//        LOGGER.debug("writeToNBT");
        super.writeToNBT(compound);
        compound.setShort(NBT_TICKS_TO_CRAFT, (short) _ticksToCraft);
        compound.setShort(NBT_TICKS_CRAFTED, (short) _ticksCrafted);
        NBTTagList nbtItemList = new NBTTagList();

        for (int i = 0; i < _craftingMatrix.getSizeInventory(); ++i)
        {
            if (_craftingMatrix.getStackInSlot(i) != null)
            {
//                LOGGER.debug("Adding slot=" + i + ", content:" + _craftingMatrix.getStackInSlot(i));
                NBTTagCompound nbtTagCompound = new NBTTagCompound();
                nbtTagCompound.setByte(NBT_SLOT, (byte)i);
                _craftingMatrix.getStackInSlot(i).writeToNBT(nbtTagCompound);
                nbtItemList.appendTag(nbtTagCompound);
            }
        }

        if (_craftOutput.getStackInSlot(0) != null)
        {
//            LOGGER.debug("Adding craftingOutput=" + _craftOutput.getStackInSlot(0));
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            nbtTagCompound.setByte(NBT_SLOT, (byte)9);
            _craftOutput.getStackInSlot(0).writeToNBT(nbtTagCompound);
            nbtItemList.appendTag(nbtTagCompound);
        }

        if (_controls.getStackInSlot(0) != null)
        {
//            LOGGER.debug("Adding controls=" + _controls.getStackInSlot(0));
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            nbtTagCompound.setByte(NBT_SLOT, (byte)10);
            _controls.getStackInSlot(0).writeToNBT(nbtTagCompound);
            nbtItemList.appendTag(nbtTagCompound);
        }

        compound.setTag(NBT_ITEMS, nbtItemList);

        if (hasCustomName())
        {
            compound.setString(NBT_CUSTOM_NAME, _customName);
        }

        LOGGER.debug("Finished writing NBT: " + compound);
    }

    @Override
    public void setWorldObj(World worldIn)
    {
        //LOGGER.debug("setWorldObj: " + worldIn);
        super.setWorldObj(worldIn);
        if (worldIn != null && !worldIn.isRemote)
        {
            _workbench = new Workbench(worldIn);
            _broadcastHelper = new Broadcaster.BroadcastHelper(worldIn.provider.getDimensionId());
        }
        else
        {
            if (_workbench != null)
                _workbench.terminate();
            _workbench = null;
            _broadcastHelper = null;
        }
    }

    //=============================================================================================
    // ##### ##### #   # #   # ##### #   # #####  ###  ####  #   #
    //   #     #   ##  # #   # #     ##  #   #   #   # #   #  # #
    //   #     #   # # #  # #  ####  # # #   #   #   # ####    #
    //   #     #   #  ##  # #  #     #  ##   #   #   # #   #   #
    // ##### ##### #   #   #   ##### #   #   #    ###  #   #   #

    // We wrap three other inventories

    @Override
    public int getSizeInventory()
    {
        // Crafting grid, result, output, control
        // 0-8: grid
        // 9: result
        // 10: output
        // 11: controls
        return 12;
    }

    @Override
    public ItemStack getStackInSlot(int i)
    {
        if (i < 9)
            return _craftingMatrix.getStackInSlot(i);
        else if (i == 9)
            return _craftResult.getStackInSlot(0);
        else if (i == 10)
            return _craftOutput.getStackInSlot(0);
        else if (i == 11)
            return _controls.getStackInSlot(0);

        return null;
    }

    @Override
    public ItemStack decrStackSize(int i, int i1)
    {
        if (i < 9)
            return _craftingMatrix.decrStackSize(i, i1);
        else if (i == 9)
            return _craftResult.decrStackSize(0, i1);
        else if (i == 10)
            return _craftOutput.decrStackSize(0, i1);
        else if (i == 11)
            return _controls.decrStackSize(0, i1);

        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int i)
    {
        // Do we need this if we don't dump things??
        if (i < 9)
            return _craftingMatrix.getStackInSlotOnClosing(i);
        else if (i == 9)
            return _craftResult.getStackInSlotOnClosing(0);
        else if (i == 10)
            return _craftOutput.getStackInSlotOnClosing(0);
        else if (i == 11)
            return _controls.getStackInSlotOnClosing(0);

        return null;
    }

    @Override
    public void setInventorySlotContents(int i, ItemStack itemStack)
    {
        if (i < 9)
            _craftingMatrix.setInventorySlotContents(i, itemStack);
        else if (i == 9)
            _craftResult.setInventorySlotContents(0, itemStack);
        else if (i == 10)
            _craftOutput.setInventorySlotContents(0, itemStack);
        else if (i == 11)
            _controls.setInventorySlotContents(0, itemStack);
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer entityPlayer)
    {
        return true;
    }

    @Override
    public void openInventory(EntityPlayer entityPlayer)
    {
    }

    @Override
    public void closeInventory(EntityPlayer entityPlayer)
    {
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemStack)
    {
        // NOTE: We don't allow inserting in result or output.
        if (i < 9)
        {
            // We only allow more of the same
            ItemStack stack = _craftingMatrix.getStackInSlot(i);
            return stack != null && stack.isItemEqual(itemStack) &&
                    (stack.stackSize < stack.getMaxStackSize());
        }
        else if(i == 11)
        {
            return (itemStack.getItem() == Items.redstone);
        }

        return false;
    }

    @Override
    public int getField(int id)
    {
        switch (id)
        {
            case 0:
                return _ticksToCraft;
            case 1:
                return _ticksCrafted;
            case 2:
                return _enabled ? 1 : 0;
        }
        return 0;
    }

    @Override
    public void setField(int id, int value)
    {
        switch (id)
        {
            case 0:
                _ticksToCraft = value;
                break;
            case 1:
                _ticksCrafted = value;
                break;
            case 2:
                _enabled = value == 1;
                break;
        }
    }

    @Override
    public int getFieldCount()
    {
        return 3;
    }

    @Override
    public void clear()
    {

    }

    //=============================================================================================
    // #####  ###  ##### ####  ##### ####  ##### #   # #   # ##### #   # #####  ###  ####  #   #
    //   #   #       #   #   # #     #   #   #   ##  # #   # #     ##  #   #   #   # #   #  # #
    //   #    ###    #   #   # ####  #   #   #   # # #  # #  ####  # # #   #   #   # ####    #
    //   #       #   #   #   # #     #   #   #   #  ##  # #  #     #  ##   #   #   # #   #   #
    // #####  ###  ##### ####  ##### ####  ##### #   #   #   ##### #   #   #    ###  #   #   #

    @Override
    public int[] getSlotsForFace(EnumFacing direction)
    {
        if (direction == EnumFacing.DOWN)
            return new int[] {10};

        return new int[] {0,1,2,3,4,5,6,7,8};
    }

    @Override
    public boolean canInsertItem(int index, ItemStack stack, EnumFacing direction)
    {
        return isItemValidForSlot(index, stack);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction)
    {
        // Sure, they are are allowed to take out whatever they want.
        return true;
    }


    //=============================================================================================
    // ##### #   #  ###  ####  #     ####  #   #  ###  #   # #####  ###  ####  #     #####
    //   #   #   # #   # #   # #     #   # ##  # #   # ## ## #     #   # #   # #     #
    //   #   # # # #   # ####  #     #   # # # # ##### # # # ####  ##### ##### #     ####
    //   #   ## ## #   # #   # #     #   # #  ## #   # #   # #     #   # #   # #     #
    // ##### #   #  ###  #   # ##### ####  #   # #   # #   # ##### #   # ####  ##### #####

    // We can redirect to the
    @Override
    public String getName()
    {
        return hasCustomName() ? _customName : "container.beaconWorkbench";
    }

    @Override
    public boolean hasCustomName()
    {
        return _customName != null && _customName.length() > 0;
    }

//    public IChatComponent getDisplayName()
//    {
//        return new ChatComponentTranslation("container.beaconWorkbench");
//    }

    //=============================================================================================
    // IInteractionObject
    //=============================================================================================

    @Override
    public String getGuiID()
    {
        return "vassal:beaconWorkbench";
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
        // Always update listeners even if not making stuff
        if (!worldObj.isRemote && _workbench != null)
            _workbench.update();

        // If we are disabled, we don't do anything
        if (!_enabled)
            return;

        boolean wasCraftingFlag = isCrafting();
        boolean dirtyFlag = false;
        if (isCrafting())
        {
            ++_ticksCrafted;
            dirtyFlag = true;
        }

        if (!worldObj.isRemote)
        {

            if (_workbench != null)
                _workbench.update();

            // Make sure we are still in a valid crafting configuration
            if (!canCraft())
            {
                _ticksCrafted = 0;
                _ticksToCraft = 0;
                dirtyFlag = wasCraftingFlag;
            }
            else
            {
                if (isCrafting())
                {
                    if (_ticksCrafted <= _ticksToCraft)
                        return;

                    // Craft it.
                    //LOGGER.debug("Crafting final item");
                    craftItem();
                    dirtyFlag = true;
                }

                // Now, let's see if we can craft again
                if (canCraft())
                {
                    // Start up the crafting
                    ItemStack newStack = CraftingManager.getInstance().findMatchingRecipe(_craftingCore.getCraftingMatrix(), getWorld());
                    //LOGGER.debug("Started crafting: " + newStack );
                    _ticksToCraft = ticksToCraftOneItem(newStack);
                    _ticksCrafted = 0;

                    // We are always dirty because we have to update progress
                    dirtyFlag = true;
                }
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

    public boolean getEnabled()
    {
        return _enabled;
    }

    public void setEnabled(boolean enabled)
    {
        if (!enabled && _enabled)
        {
            // Disable everything
            _ticksCrafted = 0;
            _ticksToCraft = 0;
        }
        _enabled = enabled;
        markDirty();
    }

    public void toggleEnabled()
    {
        setEnabled(!_enabled);
    }

    public InventoryBeaconWorkbench makeInventory(Container container)
    {
        return new InventoryBeaconWorkbench(container);
    }

    private class InventoryBeaconWorkbench extends InventoryCrafting
    {
        private InventoryBeaconWorkbench(Container container)
        {
            super(container, 3, 3);
        }

        // TODO:  Add get field/set field to make this work right.
    }

    public ContainerCraftingCore getCraftingCore()
    {
        return _craftingCore;
    }

    public boolean isCrafting()
    {
        return _ticksToCraft > 0;
    }

    // this function indicates whether container texture should be drawn
    @SideOnly(Side.CLIENT)
    public static boolean isCrafting(TileEntityBeaconWorkbench workbench)
    {
        return workbench._ticksToCraft > 0;
    }

    public int ticksToCraftOneItem(ItemStack itemStack)
    {
        // TODO:  base on hardness or harvest level or something...
        return 200;
    }

    /**
     * @return Returns amount the percentage of progress
     */
    public float getProgressPercent()
    {
        if (_ticksToCraft == 0)
            return 0;

        return (_ticksCrafted * 1.0F)/(_ticksToCraft * 1.0F);
    }

    //=============================================================================================

    private boolean canCraft()
    {
        return canCraft(true);
    }

    private boolean canCraft(boolean requireMats)
    {
        ItemStack newStack = CraftingManager.getInstance().findMatchingRecipe(_craftingCore.getCraftingMatrix(), getWorld());
        if (newStack == null)
        {
            //LOGGER.debug("Can't craft, no recipe.");
            return false;
        }

        // Check to make sure all things have more than one
        if (requireMats)
        {
            for (int i = 0; i < 9; ++i)
            {
                ItemStack stack = _craftingMatrix.getStackInSlot(i);
                if (stack != null && stack.stackSize < 2)
                {
                    //LOGGER.debug("Can't craft, stack size < 2. slot=" + i + ", stack=" + stack);
                    return false;
                }
            }
        }

        // Make sure the thing we can make can go into the output
        ItemStack outStack = _craftOutput.getStackInSlot(0);
        if (outStack != null && !outStack.isItemEqual(newStack))
        {
            //LOGGER.debug("Can't craft, outStack not same thing. outstack=" + outStack);
            return false;
        }

        // See if we have room for more
        if (outStack != null && (outStack.stackSize + newStack.stackSize) >= outStack.getMaxStackSize())
        {
            //LOGGER.debug("Can't craft, outStack full. outstack=" + outStack);
            return false;
        }

        // We are a go!
        return true;
    }

    public void craftItem()
    {
        // If we are here there is room and we have compatible stacks.  Update sizes
        ItemStack newStack = CraftingManager.getInstance().findMatchingRecipe(_craftingMatrix, getWorld());
        ItemStack outStack = _craftOutput.getStackInSlot(0);

        if (outStack != null)
            outStack.stackSize += newStack.stackSize;
        else
            _craftOutput.setInventorySlotContents(0, newStack.copy());

        for (int i = 0; i < 9; ++i)
        {
            ItemStack stack = _craftingMatrix.getStackInSlot(i);
            if (stack != null)
                --stack.stackSize;
        }

        _ticksToCraft = 0;
        _ticksCrafted = 0;
    }

    public void blockBroken()
    {
        _workbench.terminate();
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
    public int[] getInputSlots()
    {
        return new int[] {0,1,2,3,4,5,6,7,8};
    }

    @Override
    public int[] getOutputSlots()
    {
        return new int[] {10};
    }

    @Override
    public ItemStack mergeIntoBestSlot(ItemStack stack)
    {
        List<ItemStack> candidates = new ArrayList<ItemStack>();
        int size = 0;
        for (int i = 0; i < 9; ++i)
        {
            ItemStack tmp = _craftingMatrix.getStackInSlot(i);
            if (tmp != null && tmp.isItemEqual(stack))
            {
                candidates.add(tmp);
                size += tmp.stackSize;
            }
        }

        if (candidates.isEmpty())
            return stack;

        stack.stackSize = balanceStacks(candidates, size, stack.getMaxStackSize() * candidates.size(),
                stack.stackSize);

        // If we didn't get rid of everything then see if we can dump into the first.
        if (stack.stackSize > 0)
            VassalUtils.mergeStacks(candidates.get(0), stack);

        if (stack.stackSize == 0)
            return null;

        return stack;
    }

    // Rebalance the recipes stacks as best we can.
    public static int balanceStacks(List<ItemStack> stacks, int existing, int allowable, int available)
    {
        int added = allowable - existing;
        if (added > available)
            added = available;

        existing += added;

        // Split
        int share = existing / stacks.size();
        int remainder = existing - (share * stacks.size());
        for (ItemStack stack : stacks)
        {
            if (share < existing)
            {
                if (remainder > 0)
                {
                    stack.stackSize = share + 1;
                    existing -= share + 1;
                    remainder --;
                }
                else
                {
                    stack.stackSize = share;
                    existing -= share;
                }
            }
            else
            {
                // This should be last one
                stack.stackSize = existing;
                existing = 0;
            }
        }

        return existing;
    }




    //---------------------------------------------------------------------------------------------

    // TaskMaster Workbench
    private class Workbench extends BeaconBase
    {
        Workbench(World world)
        {
            super(world);
        }

        @Override
        protected IMessager getSender()
        {
            return TileEntityBeaconWorkbench.this;
        }

        @Override
        protected int concurrentWorkerCount()
        {
            // We just want a few
            return 1;
        }

        @Override
        protected void handleMessage(Message msg)
        {
            if (msg instanceof MessageWorkerAvailability)
                handleWorkerAvailability((MessageWorkerAvailability)msg);
            else if (msg instanceof MessageIsStorageAvailable)
                handleStorageAvailable((MessageIsStorageAvailable)msg);
            else if (msg instanceof MessageItemRequest)
                handleItemRequest((MessageItemRequest)msg);
        }

        private void handleWorkerAvailability(MessageWorkerAvailability msg)
        {
            if (!haveFreeWorkerSlots())
                return;

                // Only ask for things if we have a valid config.
            if (!canCraft(false))
                return;

            // If we have anything at 1, build matcher for that.
            ItemStackMatcher matcher = matcherRequiredInputs(1);
            if (matcher != null)
            {
                TRPutInInventory req = new TRPutInInventory(TileEntityBeaconWorkbench.this,
                        msg.getSender(), msg.getTransactionID(),
                        Priority.getWorkbenchInventoryOutRequestValue(), matcher, 8);
                _broadcastHelper.postMessage(req);
                return;
            }

            // Otherwise we can use a little of everything
            matcher = matcherRequiredInputs(8);
            if (matcher != null)
            {
                TRPutInInventory req = new TRPutInInventory(TileEntityBeaconWorkbench.this,
                        msg.getSender(), msg.getTransactionID(),
                        Priority.getWorkbenchInventoryLowRequestValue(), matcher, 8);
                _broadcastHelper.postMessage(req);
            }
        }

        private void handleStorageAvailable(MessageIsStorageAvailable msg)
        {
            ItemStackMatcher matcher = matcherRequiredInputs(32);
            if (matcher != null)
            {
                TRPutInInventory req = new TRPutInInventory(TileEntityBeaconWorkbench.this,
                        msg.getSender(), msg.getTransactionID(),
                        Priority.getWorkbenchStorageAvailValue(), matcher, 32);
                _broadcastHelper.postMessage(req);
            }
        }

        private void handleItemRequest(MessageItemRequest msg)
        {
            // Anything in the out slot they can have
            ItemStack stack = _craftOutput.getStackInSlot(0);
            if (stack != null && msg.getMatcher().matches(stack))
            {
                TRGetFromInventory req = new TRGetFromInventory(TileEntityBeaconWorkbench.this,
                        msg.getSender(), msg.getTransactionID(),
                        Priority.getWorkbenchItemRequestValue(), new ItemStackMatcher(stack),
                        msg.getQuantity());
                _broadcastHelper.postMessage(req);
            }
        }
    }



    //---------------------------------------------------------------------------------------------
    private ItemStackMatcher matcherRequiredInputs(int size)
    {
        ItemStackMatcher matcher = new ItemStackMatcher();
        int count = 0;
        for (int i = 0; i < 9; ++i)
        {
            ItemStack stack = _craftingMatrix.getStackInSlot(i);
            if (stack != null && stack.stackSize <= size)
            {
                matcher.add(stack);
                count++;
            }
        }

        return count > 0 ? matcher : null;
    }

    @Override
    public String toString()
    {
        return "TEBWorkbench@" + Integer.toHexString(this.hashCode()) + "{}";
    }


    //---------------------------------------------------------------------------------------------

    private final InventoryCrafting     _craftingMatrix;
    private final IInventory            _craftResult;
    private final IInventory            _craftOutput;
    private final IInventory            _controls;
    private final ContainerCraftingCore _craftingCore;

    // State Tracker
    private int _ticksToCraft;
    private int _ticksCrafted;
    private boolean _enabled = false;

    private String _customName;

    private static final String NBT_ITEMS = "Items";
    private static final String NBT_SLOT = "Slot";
    private static final String NBT_CUSTOM_NAME = "CustomName";
    private static final String NBT_TICKS_TO_CRAFT = "ticksToCraft";
    private static final String NBT_TICKS_CRAFTED = "ticksCrafted";

    private Workbench _workbench;
    private Broadcaster.BroadcastHelper _broadcastHelper;

    private static final Logger LOGGER = LogManager.getLogger();
}

