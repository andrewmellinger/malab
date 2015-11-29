package com.crashbox.malab.workdroid;

import com.crashbox.malab.MALabMain;
import com.crashbox.malab.ai.EntityAIFollowPlayer;
import com.crashbox.malab.ai.EntityAIWorkDroid;
import com.crashbox.malab.network.MessageWorkDroidEffects;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

/**
 * Copyright 2015 Andrew o. Mellinger
 */
public class EntityWorkDroid extends EntityCreature
{
    public EntityWorkDroid(World world)
    {
        super(world);
        setupAI();

        _toolStacks[0] = new ItemStack(Items.stone_pickaxe);
        _toolStacks[1] = new ItemStack(Items.stone_axe);
        _toolStacks[2] = new ItemStack(Items.stone_shovel);
        _toolStacks[3] = new ItemStack(Items.stone_sword);

        _fuelStack = null;

        // Debugging
//        _carryCapacity = 4;
//        _workSpeedFactor = 0.5F;

        // Release & testing
        _carryCapacity = 16;
        _workSpeedFactor = 0.75F;

        // Note on total speed (movementSpeed * factor)
        // 0.16 is pretty slow ang good for debugging
        // 0.25 is kinda quick, maybe too quick
        // _moveSpeedFactor = 1.25D;  // 1.25 * 0.20 = 0.25 -- pretty zippy
        _moveSpeedFactor = 1.0D;      // 1.0  * 0.20 = 0.20 -- Little slower than zombie
        // _moveSpeedFactor = 0.75D;  // 0.75 * 0.20 = 0.15 -- good for debugging
    }

    // NOTE This is NOT the same as movement speed. This is a scalar (factor) applied to the movement speed.
    public double getSpeedFactor()
    {
        if (!hasFuel())
        {
            return _moveSpeedFactor * 0.5;
        }
        return _moveSpeedFactor;
    }

    // you don't have to call this as it is called automatically during EntityLiving subclass creation
    @Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();

        // standard attributes registered to EntityLivingBase
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(40.0D);
        getEntityAttribute(SharedMonsterAttributes.knockbackResistance).setBaseValue(0.8D);
        getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(64.0D);

        // IMPORTANT:  This is not the same as the multiplier that is specified in "tryMoveTo".
        // These two numbers are multiplied together.  Zombie is 0.23000000417232513D
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.20D);

        // need to register any additional attributes
        //getAttributeMap().registerAttribute(SharedMonsterAttributes.attackDamage);
        //getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(4.0D);
    }

    protected void entityInit()
    {
        super.entityInit();
        this.dataWatcher.addObject(20, 0);
    }

    @Override
    protected boolean canDespawn()
    {
        return false;
    }

    @Override
    protected void despawnEntity()
    {
        // Nothing special to do.
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound)
    {
        super.writeEntityToNBT(compound);

        compound.setInteger("fuelTicks", getFuelTicks());

        if (_fuelStack != null)
        {
            NBTTagCompound fuelCompound = new NBTTagCompound();
            _fuelStack.writeToNBT(fuelCompound);
            compound.setTag("fuelStack", fuelCompound);
        }

        if (_followMeStack != null)
        {
            NBTTagCompound followMeCompount = new NBTTagCompound();
            _followMeStack.writeToNBT(followMeCompount);
            compound.setTag("followMeStack", followMeCompount);
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound)
    {
        super.readEntityFromNBT(compound);

        if (compound.hasKey("fuelTicks"))
        {
            setFuelTicks(compound.getInteger("fuelTicks"));
        }

        if (compound.hasKey("fuelStack"))
        {
            _fuelStack = ItemStack.loadItemStackFromNBT(compound.getCompoundTag("fuelStack"));
        }

        if (compound.hasKey("followMeStack"))
        {
            _followMeStack = ItemStack.loadItemStackFromNBT(compound.getCompoundTag("followMeStack"));
        }

        // We always want a custom name.  If we don't have one by now, make one!
        if (!hasCustomName())
            setCustomNameTag(makeName());
    }

    @Override
    public void onLivingUpdate()
    {
        super.onLivingUpdate();

        if (_durationTicks <= 0)
            return;

        --_durationTicks;
        showParticles(EnumParticleTypes.getParticleFromId(_effectID));
    }

    @Override
    public void onEntityUpdate()
    {
        super.onEntityUpdate();
        if (!worldObj.isRemote)
        {
            if (_fuelTicks == 0 && _fuelStack == null)
            {
                --_fuelOutTicks;
                if (_fuelOutTicks <= 0)
                {
                    sendParticleMessage(EnumParticleTypes.SPELL_MOB_AMBIENT, 12); // pretty good

                    // Other particles we've looked at.
//                    sendParticleMessage(EnumParticleTypes.PORTAL, 12);
//                    sendParticleMessage(EnumParticleTypes.SPELL_INSTANT, 12);     // ok
//                    sendParticleMessage(EnumParticleTypes.REDSTONE, 12);          // no
//                    sendParticleMessage(EnumParticleTypes.SPELL_MOB, 12);
//                    sendParticleMessage(EnumParticleTypes.EXPLOSION_HUGE, 12);    // Can't see what is going on
//                    sendParticleMessage(EnumParticleTypes.CLOUD, 12);             // White stuff
//                    sendParticleMessage(EnumParticleTypes.SMOKE_LARGE, 12);
//                    sendParticleMessage(EnumParticleTypes.ENCHANTMENT_TABLE, 12);  // maybe for healing
//                    sendParticleMessage(EnumParticleTypes.BARRIER, 12);       // Big "no" symbol
//                    sendParticleMessage(EnumParticleTypes.SUSPENDED, 12);     // Nothing
//                    sendParticleMessage(EnumParticleTypes.TOWN_AURA, 12);     // Bedrock effect
//                    sendParticleMessage(EnumParticleTypes.SPELL_WITCH, 12);   // Purple effect.
                    _fuelOutTicks = 10;
                }
            }
            _fuelOutTicks = 0;
        }
    }

    public void showParticles(EnumParticleTypes particleType)
    {
        double var1 = this.rand.nextGaussian() * 0.02D;
        double var3 = this.rand.nextGaussian() * 0.02D;
        double var5 = this.rand.nextGaussian() * 0.02D;
        this.worldObj.spawnParticle(particleType,
                this.posX + (double) (this.rand.nextFloat() * this.width * 2.0F) - (double) this.width,
                this.posY + 0.5D + (double) (this.rand.nextFloat() * this.height),
                this.posZ + (double) (this.rand.nextFloat() * this.width * 2.0F) - (double) this.width,
                var1, var3, var5);
    }

    /**
     * Used to set a particle effect on the bot.  This is for things like visual feedback
     * on healing, out of fuel, etc.
     *
     * @param particleID    The particle ID.
     * @param durationTicks How many ticks to show it for.
     */
    public void setParticleEffect(int particleID, int durationTicks)
    {
        _effectID = particleID;
        _durationTicks = durationTicks;
    }

    // This is server side
    public void sendParticleMessage(EnumParticleTypes type, int durationTicks)
    {
        MessageWorkDroidEffects msg = new MessageWorkDroidEffects();
        msg.setup(getEntityWorld().provider.getDimensionId(),
                getEntityId(),
                type.getParticleID(),
                durationTicks);
        MALabMain.NETWORK.sendToAll(msg);
    }

    //=============================================================================================

    protected void setupAI()
    {
        // http://jabelarminecraft.blogspot.com/p/minecraft-forge-1721710-custom-entity-ai.html
        //getNavigator().
        clearAITasks();

        int priority = 0;
        tasks.addTask(priority++, new EntityAISwimming(this));

        // We should follow first
        tasks.addTask(priority++, new EntityAIFollowPlayer(this, 2, 32));

        // We wander slower than we normally move
        //tasks.addTask(priority++, new EntityAIWander(this, 0.2D, 10));
        _workDroid = new EntityAIWorkDroid(this);
        tasks.addTask(priority, _workDroid);
        //tasks.addTask(priority++, _workDroid);

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
     * @return A performance modifier for mining speed.  Higher is faster.
     */
    public float getWorkSpeedFactor()
    {
        if (!hasFuel())
            return _workSpeedFactor * 0.5F;

        return _workSpeedFactor;
    }

    //=============================================================================================

    public void resume()
    {
        _workDroid.resume();
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
        return held != null && held.stackSize >= _carryCapacity;

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
            if (canToolHarvestBlock(getEntityWorld(), pos, stack))
            {
                return stack;
            }
        }

        return null;
    }

    // Copy paste from ForgeHooks!
    public static boolean canToolHarvestBlock(IBlockAccess world, BlockPos pos, ItemStack stack)
    {
        IBlockState state = world.getBlockState(pos);
        state = state.getBlock().getActualState(state, world, pos);
        String tool = state.getBlock().getHarvestTool(state);
        return !(stack == null || tool == null) &&
                stack.getItem().getHarvestLevel(stack, tool) >= state.getBlock().getHarvestLevel(state);
    }


    //=============================================================================================


    public boolean hasFuel()
    {
        // If we have fuel ticks or spare fuel then we can run
        return (_fuelTicks > 0);
    }

    public boolean burnFuel()
    {
        if (_fuelTicks > 0)
        {
            setFuelTicks(_fuelTicks - 1);
            return true;
        }

        return false;
    }

    public boolean hasFuel(int qty)
    {
        if (_fuelTicks > qty)
            return true;

        qty -= _fuelTicks;

        if (_fuelStack != null)
        {
            int ticks = TileEntityFurnace.getItemBurnTime(_fuelStack);
            for (int i = 0; i < _fuelStack.stackSize; ++i)
            {
                if (ticks > qty)
                    return true;

                qty -= ticks;
            }
        }

        // We ran out of fuel to check.
        return false;
    }

    public int burnFuel(int qty)
    {
        int finalFuelTicks = _fuelTicks;
        if (qty < finalFuelTicks)
        {
            finalFuelTicks -= qty;
            qty = 0;
        }
        else
        {
            qty -= finalFuelTicks;
            finalFuelTicks = 0;

            if (_fuelStack != null)
            {
                int ticks = TileEntityFurnace.getItemBurnTime(_fuelStack);
                while (_fuelStack != null)
                {
                    _fuelStack.stackSize -= 1;
                    if (_fuelStack.stackSize == 0)
                        _fuelStack = null;

                    if (ticks > qty)
                    {
                        finalFuelTicks = ticks - qty;
                        qty = 0;
                        break;
                    }

                    qty -= ticks;
                }
            }
        }
        setFuelTicks(finalFuelTicks);

        // We ran out of fuel so return what we couldn't do.
        return qty;
    }

    public void ensureFuel()
    {
        if (_fuelTicks == 0)
        {
            if (_fuelStack != null)
            {
                setFuelTicks(TileEntityFurnace.getItemBurnTime(_fuelStack));
                //debugLog("Setting fuel ticks: " + _fuelTicks);
                _fuelStack.stackSize--;
                if (_fuelStack.stackSize == 0)
                    _fuelStack = null;
            }
        }
    }


    public boolean needFuel()
    {
        return (_fuelStack == null || _fuelStack.stackSize < 4);
    }

    public ItemStack getFuelStack()
    {
        return _fuelStack;
    }

    public void setFuelStack(ItemStack fuelStack)
    {
        _fuelStack = fuelStack;
    }

    // Should only be called client side?
    public int getFuelSecs()
    {
        if (getEntityWorld().isRemote)
            return getDataWatcher().getWatchableObjectInt(20);
        else
            return _fuelTicks / 20;
    }

    private int getFuelTicks()
    {
        return _fuelTicks;
    }

    private void setFuelTicks(int fuelTicks)
    {
        _fuelTicks = fuelTicks;

        if (!getEntityWorld().isRemote)
        {
            if (_lastFuelSecs != _fuelTicks / 20)
            {
                _lastFuelSecs = _fuelTicks / 20;
                getDataWatcher().updateObject(20, _lastFuelSecs);
            }
        }
    }

    //=============================================================================================
    public ItemStack getFollowMeStack()
    {
        return _followMeStack;
    }

    public void setFollowMeStack(ItemStack stack)
    {
        _followMeStack = stack;
    }

    public void setFollowPlayer(EntityPlayer player)
    {
        _followPlayer = player;
    }

    public EntityPlayer getFollowPlayer()
    {
        if (_followMeStack == null)
            return null;

        return _followPlayer;
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
        _workDroid.cancelAndPause();
        player.openGui(MALabMain.instance,
                MALabMain.GUI_ENUM.DROID.ordinal(),
                getEntityWorld(),
                getEntityId(),
                0,
                0);

        return true;
    }

    //=============================================================================================

    @Override
    public String toString()
    {
        return getCustomNameTag() + '{' +
                "fuel=" + _fuelTicks + '/' + (_fuelStack == null ? 0 : _fuelStack.stackSize) +
                '}';
    }

    @Override
    public boolean isPotionApplicable(PotionEffect p_70687_1_)
    {
        return false;
    }

    //=============================================================================================
    // NAMING

    private static String makeName()
    {
        // We have three formats that is four characters and a dash.  The dash can be anywhere inside
        // but the other characters are [A-Z0-9] except the first which is just [A-Z].

        Random rand = new Random();
        return String.format(generateFormat(rand),
                generateChar(rand),
                generateCharOrDigit(rand),
                generateCharOrDigit(rand),
                generateCharOrDigit(rand));
    }

    private static String generateFormat(Random rand)
    {
        return NAME_FORMATS[rand.nextInt(3)];
    }

    private static char generateChar(Random rand)
    {
        return LETTER_CHARS.charAt(rand.nextInt(26));
    }

    private static char generateCharOrDigit(Random rand)
    {
        return LETTER_NUM_CHARS.charAt(rand.nextInt(36));
    }

    public void setUpCustomName()
    {
        if (!hasCustomName())
            setCustomNameTag(makeName());
    }

    //=============================================================================================

    // Our pile of tools
    private ItemStack[] _toolStacks = new ItemStack[4];

    // Fuel management
    private ItemStack _fuelStack;
    private int _fuelTicks = 0;
    private int _lastFuelSecs = 0;

    private ItemStack _followMeStack;
    private EntityPlayer _followPlayer;

    // How many things we can carry.
    private int _carryCapacity;

    // Divider on time.  Higher is faster.  The player (steve) is around 1.0
    private float _workSpeedFactor;

    // Factor applied to base move speed
    private double _moveSpeedFactor;

    // For handling particle effects
    private int _effectID;
    private int _durationTicks;
    private int _fuelOutTicks;

    // We use this alot
    private EntityAIWorkDroid _workDroid;


    private static final String[] NAME_FORMATS = new String[]{"%c-%c%c%c", "%c%c-%c%c", "%c%c%c-%c"};
    private static final String LETTER_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LETTER_NUM_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

}
