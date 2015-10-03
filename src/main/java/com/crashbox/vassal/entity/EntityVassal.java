package com.crashbox.vassal.entity;

import com.crashbox.vassal.VassalMain;
import com.crashbox.vassal.ai.EntityAIVassal;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.IExtendedEntityProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.tools.Tool;


/**
 * Copyright 2015 Andrew o. Mellinger
 */
public class EntityVassal extends EntityCreature
{
    public EntityVassal(World world)
    {
        super(world);
        setupAI();
        registerExtendedProperties("vassal", new ExtendedProps());

        _toolStacks[0] = new ItemStack(Items.stone_pickaxe);
        _toolStacks[1] = new ItemStack(Items.stone_axe);
        _toolStacks[2] = new ItemStack(Items.stone_shovel);
        _toolStacks[3] = new ItemStack(Items.stone_sword);

        _fuelStack = null;
    }

    public double getSpeed()
    {
        // TODO: Somehow movement speed got set to 0.6 so we need to do this manually
        //return 0.4D;
        return getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue();
    }

    // you don't have to call this as it is called automatically during EntityLiving subclass creation
    @Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();

        // standard attributes registered to EntityLivingBase
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(40.0D);
//        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(1.0D);
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.4D);
//        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.6D);
        getEntityAttribute(SharedMonsterAttributes.knockbackResistance).setBaseValue(0.8D);
        getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(64.0D);

        // need to register any additional attributes
        //getAttributeMap().registerAttribute(SharedMonsterAttributes.attackDamage);
        //getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(4.0D);
    }

//    @Override
//    protected boolean isAIEnabled()
//    {
//        return true;
//    }

    @Override
    protected boolean canDespawn()
    {
        return false;
    }

    @Override
    protected void despawnEntity()
    {
//        LOGGER.debug(getCustomNameTag() + " Someone called despawn entity!!!");
//        super.despawnEntity();
    }

    private class ExtendedProps implements IExtendedEntityProperties
    {
        @Override
        public void saveNBTData(NBTTagCompound compound)
        {
            compound.setInteger("fuelTicks", _vassalAI.getFuelTicks());

            if (_fuelStack != null)
            {
                NBTTagCompound fuelCompound = new NBTTagCompound();
                _fuelStack.writeToNBT(fuelCompound);
                compound.setTag("fuelStack", fuelCompound);
            }
        }

        @Override
        public void loadNBTData(NBTTagCompound compound)
        {
            if (compound.hasKey("fuelTicks"))
            {
                _vassalAI.setFuelTicks(compound.getInteger("fuelTicks"));
            }

            if (compound.hasKey("fuelStack"))
            {
                _fuelStack = ItemStack.loadItemStackFromNBT(compound.getCompoundTag("fuelStack"));
            }
        }

        @Override
        public void init(Entity entity, World world)
        {
            // TODO:  What do we do here???
        }
    }
    
    
    //=============================================================================================



    protected void setupAI()
    {
        // http://jabelarminecraft.blogspot.com/p/minecraft-forge-1721710-custom-entity-ai.html
        //getNavigator().
        clearAITasks();

        int priority = 0;
        tasks.addTask(priority++, new EntityAISwimming(this));

        // We wander slower than we normally move
        //tasks.addTask(priority++, new EntityAIWander(this, 0.2D, 10));
        _vassalAI = new EntityAIVassal(this);
        tasks.addTask(priority++, _vassalAI);

        //targetTasks.addTask(0, new EntityAIHurtByTargetHerdAnimal(this, true));
    }

    /**
     * @return The number of things this can carry.
     */
    public int getCarryCapacity()
    {
        return _carryCapacity;
    }

    /**
     * @return A performance modifier for mining speed.  Lower faster.
     */
    public float getWorkSpeedFactor()
    {
        return _workSpeedFactor;
    }

    //=============================================================================================

    public void resume()
    {
        _vassalAI.resume();
    }

    // Inventory utils
    //=============================================================================================

    public void dropHeldItem()
    {
        ItemStack held = getHeldItem();
        setCurrentItemOrArmor(0, null);
        BlockPos pos = getPosition();
        getEntityWorld().spawnEntityInWorld(new EntityItem(getEntityWorld(), pos.getX(), pos.getY(), pos.getZ(), held));
    }

    public boolean isHeldInventoryFull()
    {
        ItemStack held = getHeldItem();
        if (held == null)
            return false;

        return held.stackSize >= _carryCapacity;
    }

    public int getHeldSize()
    {
        ItemStack held = getHeldItem();
        if (held == null)
            return 0;

        return held.stackSize;
    }

    public void placeHeldBlock(World world, BlockPos target)
    {
        // Make sure the block is air
        if (!world.isAirBlock(target))
            return;

        ItemStack held = getHeldItem();
        if (held == null || held.stackSize == 0)
            return;

        Block block = Block.getBlockFromItem(held.getItem());
        IBlockState state = block.getStateFromMeta(held.getMetadata());
        world.setBlockState(target, state);

        held.stackSize -= 1;
        if (held.stackSize == 0)
            setCurrentItemOrArmor(0, null);
    }

    //=============================================================================================
    // ToolSet

    public ItemStack findBestTool(BlockPos pos)
    {
        // Can we use the names to look up the tool?
        for (int i = 0; i < 4; ++i)
        {
            ItemStack stack = _toolStacks[i];
            if (ForgeHooks.canToolHarvestBlock(getEntityWorld(), pos, stack))
            {
                return stack;
            }
        }

        return null;
    }

    //=============================================================================================

    public ItemStack getFuelStack()
    {
        return _fuelStack;
    }

    public void setFuelStack(ItemStack fuelStack)
    {
        _fuelStack = fuelStack;
    }


    //=============================================================================================

    protected void clearAITasks()
    {
        tasks.taskEntries.clear();
        targetTasks.taskEntries.clear();
    }

    @Override
    protected boolean interact(EntityPlayer player)
    {
        _vassalAI.cancelAndPause();
        player.openGui(VassalMain.instance,
                VassalMain.GUI_ENUM.VASSAL.ordinal(),
                getEntityWorld(),
                getEntityId(),
                0,
                0);

//        if (!this.worldObj.isRemote && (this.riddenByEntity == null ||
//                this.riddenByEntity == playerEntity) && this.isTame())
//        {
//            this.horseChest.setCustomName(this.getName());
//            playerEntity.displayGUIHorse(this, this.horseChest);
//        }

        return true;
    }

    //=============================================================================================

    private ItemStack[] _toolStacks = new ItemStack[4];
    private ItemStack   _fuelStack;

    // How many things we can carry.
    private int _carryCapacity = 4;
//    private int _carryCapacity = 20;

    // Divider on time.  Higher is faster.  The player (steve) is around 1.0
    private float _workSpeedFactor = 0.5F;

    // We use this alot
    private EntityAIVassal _vassalAI;

    private static final Logger LOGGER = LogManager.getLogger();
}
