package com.crashbox.drudgemod;

import com.crashbox.drudgemod.ai.EntityAIDrudge;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.world.World;

/**
 * Copyright 2015 Andrew o. Mellinger
 */
public class EntityDrudge extends EntityCreature
//public class EntityDrudge extends EntityZombie
{
    public EntityDrudge(World world)
    {
        super(world);
        setupAI();
    }

    public double getSpeed()
    {
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
}
