package com.crashbox.vassal.workbench;

import com.crashbox.vassal.beacon.TileEntityBeaconInventory;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class TileEntityBeaconWorkbench extends TileEntityBeaconInventory //implements ISidedInventory
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
    }

    //=============================================================================================
    // ##### ##### #     ##### ##### #   # ##### ##### ##### #   #
    //   #     #   #     #     #     ##  #   #     #     #    # #
    //   #     #   #     ####  ####  # # #   #     #     #     #
    //   #     #   #     #     #     #  ##   #     #     #     #
    //   #   ##### ##### ##### ##### #   #   #   #####   #     #

    @Override
    public boolean shouldRefresh(World parWorld, BlockPos parPos,
                                 IBlockState parOldState, IBlockState parNewState)
    {
        return false;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        LOGGER.debug("readFromNBT");
        super.readFromNBT(compound);
        NBTTagList nbttaglist = compound.getTagList(NBT_ITEMS, 10);

        for (int i = 0; i < nbttaglist.tagCount(); ++i)
        {
            NBTTagCompound nbtTagCompound = nbttaglist.getCompoundTagAt(i);
            byte b0 = nbtTagCompound.getByte(NBT_SLOT);

            // This is the output slot
            if (b0 == 9)
            {
                ItemStack stack = ItemStack.loadItemStackFromNBT(nbtTagCompound);
                _craftOutput.setInventorySlotContents(0, stack);
                LOGGER.debug("craftedOutput from NBT: slot=" + b0 +", stack=" + stack);
            }
            else if (b0 >= 0 && b0 < _craftingMatrix.getSizeInventory())
            {
                ItemStack stack = ItemStack.loadItemStackFromNBT(nbtTagCompound);
                _craftingMatrix.setInventorySlotContents(b0, stack);
                LOGGER.debug("itemStack from NBT: slot=" + b0 +", stack=" + stack);
            }
        }

        _ticksToCraft = compound.getShort(NBT_TICKS_TO_CRAFT);
        _ticksCrafted = compound.getShort(NBT_TICKS_CRAFTED);

        if (compound.hasKey(NBT_CUSTOM_NAME, 8))
        {
            _customName = compound.getString(NBT_CUSTOM_NAME);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        LOGGER.debug("writeToNBT");
        super.writeToNBT(compound);
        compound.setShort(NBT_TICKS_TO_CRAFT, (short) _ticksToCraft);
        compound.setShort(NBT_TICKS_CRAFTED, (short) _ticksCrafted);
        NBTTagList nbtItemList = new NBTTagList();

        for (int i = 0; i < _craftingMatrix.getSizeInventory(); ++i)
        {
            if (_craftingMatrix.getStackInSlot(i) != null)
            {
                NBTTagCompound nbtTagCompound = new NBTTagCompound();
                nbtTagCompound.setByte(NBT_SLOT, (byte)i);
                _craftingMatrix.getStackInSlot(i).writeToNBT(nbtTagCompound);
                nbtItemList.appendTag(nbtTagCompound);
                LOGGER.debug("itemStack TO NBT: slot=" + i + ", stack=" + _craftingMatrix.getStackInSlot(i));
            }
        }

        if (_craftOutput.getStackInSlot(0) != null)
        {
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            nbtTagCompound.setByte(NBT_SLOT, (byte)9);
            _craftOutput.getStackInSlot(0).writeToNBT(nbtTagCompound);
            nbtItemList.appendTag(nbtTagCompound);
            LOGGER.debug("itemStack TO NBT: output=" + _craftResult.getStackInSlot(0));
        }

        compound.setTag(NBT_ITEMS, nbtItemList);

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
    // ##### ##### #   # #   # ##### #   # #####  ###  ####  #   #
    //   #     #   ##  # #   # #     ##  #   #   #   # #   #  # #
    //   #     #   # # #  # #  ####  # # #   #   #   # ####    #
    //   #     #   #  ##  # #  #     #  ##   #   #   # #   #   #
    // ##### ##### #   #   #   ##### #   #   #    ###  #   #   #

    // We wrap three other inventories

    @Override
    public int getSizeInventory()
    {
        // Crafting grid, result, output
        return 11;
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

        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int i)
    {
        if (i < 9)
            return _craftingMatrix.getStackInSlotOnClosing(i);
        else if (i == 9)
            return _craftResult.getStackInSlotOnClosing(0);
        else if (i == 10)
            return _craftOutput.getStackInSlotOnClosing(0);

        return null;
    }

    @Override
    public void setInventorySlotContents(int i, ItemStack itemStack)
    {
        if (i < 9)
            _craftingMatrix.setInventorySlotContents(i, itemStack);
        else if (i == 9)
            _craftResult.setInventorySlotContents(i, itemStack);
        else if (i == 10)
            _craftOutput.setInventorySlotContents(i, itemStack);
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
            return _craftingMatrix.isItemValidForSlot(i, itemStack);
        else if (i == 9)
            return false;
        else if (i == 10)
            return false;

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
        }
    }

    @Override
    public int getFieldCount()
    {
        return 2;
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

//    @Override
//    public int[] getSlotsForFace(EnumFacing enumFacing)
//    {
//        return new int[0];
//    }
//
//    @Override
//    public boolean canInsertItem(int i, ItemStack itemStack, EnumFacing enumFacing)
//    {
//        return false;
//    }
//
//    @Override
//    public boolean canExtractItem(int i, ItemStack itemStack, EnumFacing enumFacing)
//    {
//        return false;
//    }


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

    public IChatComponent getDisplayName()
    {
        return new ChatComponentTranslation("container.beaconWorkbench");
    }

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
        boolean wasCraftingFlag = isCrafting();
        boolean dirtyFlag = false;
        if (isCrafting())
        {
            ++_ticksCrafted;
            dirtyFlag = true;
        }

        if (!worldObj.isRemote)
        {
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
                    LOGGER.debug("Crafting final item");
                    craftItem();
                    dirtyFlag = true;
                }

                // Now, let's see if we can craft again
                if (canCraft())
                {
                    // Start up the crafting
                    ItemStack newStack = CraftingManager.getInstance().findMatchingRecipe(_craftingCore.getCraftingMatrix(), getWorld());
                    LOGGER.debug("Started crafting: " + newStack );
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
        return (_ticksCrafted * 1.0F)/(_ticksToCraft * 1.0F);
    }

    //=============================================================================================

    private boolean canCraft()
    {
        ItemStack newStack = CraftingManager.getInstance().findMatchingRecipe(_craftingCore.getCraftingMatrix(), getWorld());
        if (newStack == null)
        {
            //LOGGER.debug("Can't craft, no recipe.");
            return false;
        }

        // Check to make sure all things have more than one
        for (int i = 0; i < 9; ++i)
        {
            ItemStack stack = _craftingMatrix.getStackInSlot(i);
            if (stack != null && stack.stackSize < 2)
            {
                //LOGGER.debug("Can't craft, stack size < 2. slot=" + i + ", stack=" + stack);
                return false;
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
        //_workbench.terminate();
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
        return new int[0];
    }

    @Override
    public int[] getOutputSlots()
    {
        return new int[0];
    }


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
//                    availability.getAIVassal().offer(new TaskDeliver(this, TileEntityBeaconWorkbench.this,
//                            getSmeltableItemSample(), INPUT_INDEX, getSmeltableQuantityWanted()));
//                }
//            }
//        }
//    }


    //---------------------------------------------------------------------------------------------

    private final InventoryCrafting _craftingMatrix;
    private final IInventory        _craftResult;
    private final IInventory        _craftOutput;
    private final ContainerCraftingCore _craftingCore;

    // State Tracker
    private int _ticksToCraft;
    private int _ticksCrafted;

    private String _customName;

    public static final int FIELD_TICKS_TO_CRAFT = 0;
    public static final int FIELD_TICK_CRAFTED = 1;

    private static final String NBT_ITEMS = "Items";
    private static final String NBT_SLOT = "Slot";
    private static final String NBT_CUSTOM_NAME = "CustomName";
    private static final String NBT_TICKS_TO_CRAFT = "ticksToCraft";
    private static final String NBT_TICKS_CRAFTED = "ticksCrafted";

    //private Workbench _workbench;
    private static final Logger LOGGER = LogManager.getLogger();
}

