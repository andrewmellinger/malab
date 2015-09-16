package com.crashbox.vassal;

import com.crashbox.vassal.ai.EntityAIVassal;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Copyright 2015 Andrew o. Mellinger
 */
public class EntityVassal extends EntityCreature
{
    public EntityVassal(World world)
    {
        super(world);
        setupAI();
    }

    public double getSpeed()
    {
        // TODO: Somehow movement speed got set to 0.6 so we need to do this manually
        return 0.4D;
        //return getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue();
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
    protected void onDeathUpdate()
    {
        super.onDeathUpdate();

        LOGGER.debug(getCustomNameTag() + " !!!!!!!!!!!!! Entity died.");
    }

    protected void setupAI()
    {
        // http://jabelarminecraft.blogspot.com/p/minecraft-forge-1721710-custom-entity-ai.html
        //getNavigator().
        clearAITasks();

        int priority = 0;
        tasks.addTask(priority++, new EntityAISwimming(this));

        // We wander slower than we normally move
        //tasks.addTask(priority++, new EntityAIWander(this, 0.2D, 10));

        tasks.addTask(priority++, new EntityAIVassal(this));

        //targetTasks.addTask(0, new EntityAIHurtByTargetHerdAnimal(this, true));
    }

    protected void clearAITasks()
    {
        tasks.taskEntries.clear();
        targetTasks.taskEntries.clear();
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

    // Inventory utils


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


    // How many things we can carry.
    private int _carryCapacity = 4;

    // Multiplier on mining speed, a percentage.  Base vassal is 1.0;  Lower faster.
    private float _workSpeedFactor = 1.0F;


    private static final Logger LOGGER = LogManager.getLogger();
}
