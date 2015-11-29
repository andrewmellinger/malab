package com.crashbox.malab.network;

import com.crashbox.malab.workdroid.EntityWorkDroid;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class MessageWorkDroidEffects implements IMessage
{
    @Override
    public void fromBytes(ByteBuf buf)
    {
        _worldID = ByteBufUtils.readVarShort(buf);
        _entityID = ByteBufUtils.readVarShort(buf);
        _particleID = ByteBufUtils.readVarShort(buf);
        _durationTicks = ByteBufUtils.readVarShort(buf);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        ByteBufUtils.writeVarShort(buf, _worldID);
        ByteBufUtils.writeVarShort(buf, _entityID);
        ByteBufUtils.writeVarShort(buf, _particleID);
        ByteBufUtils.writeVarShort(buf, _durationTicks);
    }

    // Called to set all the values.
    public void setup(int worldID, int entityID, int effectID, int delayMS)
    {
        _worldID = worldID;
        _entityID = entityID;
        _particleID = effectID;
        _durationTicks = delayMS;
    }

    public int getWorldID()
    {
        return _worldID;
    }

    public int getEntityID()
    {
        return _entityID;
    }

    public int getParticleID()
    {
        return _particleID;
    }

    public int getDurationTicks()
    {
        return _durationTicks;
    }

    @Override
    public String toString()
    {
        return "MessageWorkDroidEffects{" +
                "_worldID=" + _worldID +
                ", _entityID=" + _entityID +
                ", _particleID=" + _particleID +
                ", _durationTicks=" + _durationTicks +
                '}';
    }

    public static class Handler implements IMessageHandler<MessageWorkDroidEffects, IMessage>
    {
        @Override
        public IMessage onMessage(final MessageWorkDroidEffects message, MessageContext ctx)
        {
            //IThreadListener mainThread = (WorldServer) ctx.getServerHandler().playerEntity.worldObj;
            //mainThread.addScheduledTask(new Runnable()
            Minecraft.getMinecraft().addScheduledTask(new Runnable()
            {
                @Override
                public void run()
                {
                    //World world = DimensionManager.getWorld(message.getWorldID());
                    //Entity entity = world.getEntityByID(message.getEntityID());
                    Entity entity = Minecraft.getMinecraft().theWorld.getEntityByID(message.getEntityID());

                    // This also handles null entity
                    if (entity instanceof EntityWorkDroid)
                    {
                        ((EntityWorkDroid) entity).setParticleEffect(message.getParticleID(), message.getDurationTicks());
                    }
                }
            });
            return null;
        }
    }

    private int _worldID;
    private int _entityID;
    private int _particleID;
    private int _durationTicks;

    private static final Logger LOGGER = LogManager.getLogger();
}
