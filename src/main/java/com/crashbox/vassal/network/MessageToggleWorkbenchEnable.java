package com.crashbox.vassal.network;

import com.crashbox.vassal.VassalMain;
import com.crashbox.vassal.workbench.TileEntityBeaconWorkbench;
import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class MessageToggleWorkbenchEnable implements IMessage
{
    @Override
    public void fromBytes(ByteBuf buf)
    {
        // NOTE:  We have to use 5 because of being signed.  It isn't smart enough to deal with signed values.
        _worldID = ByteBufUtils.readVarShort(buf);
        int x = ByteBufUtils.readVarInt(buf, 5);
        int y = ByteBufUtils.readVarInt(buf, 5);
        int z = ByteBufUtils.readVarInt(buf, 5);
        _pos = new BlockPos(x, y, z);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        // NOTE:  We have to use 5 because of being signed.  It isn't smart enough to deal with signed values.
        VassalMain.LOGGER.debug("toBytes pos=" + _pos);
        ByteBufUtils.writeVarShort(buf, _worldID);
        ByteBufUtils.writeVarInt(buf, _pos.getX(), 5);
        ByteBufUtils.writeVarInt(buf, _pos.getY(), 5);
        ByteBufUtils.writeVarInt(buf, _pos.getZ(), 5);
    }

    public int getWorldID()
    {
        return _worldID;
    }

    public void setWorldID(int worldID)
    {
        _worldID = worldID;
    }

    public BlockPos getPos()
    {
        return _pos;
    }

    public void setPos(BlockPos pos)
    {
        _pos = pos;
    }

    //---------------------------------

    public static class Handler implements IMessageHandler<MessageToggleWorkbenchEnable, IMessage>
    {
        @Override
        public IMessage onMessage(final MessageToggleWorkbenchEnable message, MessageContext ctx)
        {
            // or Minecraft.getMinecraft() on the client
            IThreadListener mainThread = (WorldServer) ctx.getServerHandler().playerEntity.worldObj;
            mainThread.addScheduledTask(new Runnable()
            {
                @Override
                public void run()
                {
                    World world = DimensionManager.getWorld(message.getWorldID());
                    TileEntity entity = world.getTileEntity(message.getPos());
                    if (entity instanceof TileEntityBeaconWorkbench)
                        ((TileEntityBeaconWorkbench)entity).toggleEnabled();
                }
            });
            return null;
        }
    }

    private int _worldID;
    private BlockPos _pos;
}
