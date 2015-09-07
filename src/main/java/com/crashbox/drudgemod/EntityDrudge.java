package com.crashbox.drudgemod;

import com.crashbox.drudgemod.ai.EntityAIDrudge;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Copyright 2015 Andrew o. Mellinger
 */
public class EntityDrudge extends EntityCreature
{
    public EntityDrudge(World world)
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

        tasks.addTask(priority++, new EntityAIDrudge(this));

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

    // How many things we can carry.
    private int _carryCapacity = 4;

    // Multiplier on mining speed, a percentage.  Base drudge is 1.0;  Lower faster.
    private float _workSpeedFactor = 1.0F;


    private static final Logger LOGGER = LogManager.getLogger();
}
