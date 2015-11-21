package com.crashbox.mal;

import com.crashbox.mal.chest.ContainerAutoChest;
import com.crashbox.mal.chest.GuiAutoChest;
import com.crashbox.mal.chest.TileEntityAutoChest;
import com.crashbox.mal.workdroid.ContainerEntityWorkDroid;
import com.crashbox.mal.workdroid.EntityWorkDroid;
import com.crashbox.mal.workdroid.GuiEntityWorkDroid;
import com.crashbox.mal.workdroid.InventoryEntityWorkDroid;
import com.crashbox.mal.forester.ContainerAutoForester;
import com.crashbox.mal.forester.GuiAutoForester;
import com.crashbox.mal.furnace.ContainerAutoFurnace;
import com.crashbox.mal.furnace.GuiAutoFurnace;
import com.crashbox.mal.furnace.TileEntityAutoFurnace;
import com.crashbox.mal.quarry.ContainerAutoQuarry;
import com.crashbox.mal.quarry.GuiAutoQuarry;
import com.crashbox.mal.workbench.ContainerAutoWorkbench;
import com.crashbox.mal.workbench.GuiAutoWorkbench;
import com.crashbox.mal.workbench.TileEntityAutoWorkbench;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
@SideOnly(Side.CLIENT)
public class GuiHandlerMAL implements IGuiHandler
{
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player,
                                      World world, int x, int y, int z)
    {
        if (ID == MALMain.GUI_ENUM.DROID.ordinal())
        {
            Entity entity = world.getEntityByID(x);
            if (entity instanceof EntityWorkDroid)
            {
                InventoryEntityWorkDroid inventory = new InventoryEntityWorkDroid((EntityWorkDroid) entity);
                return new ContainerEntityWorkDroid(player.inventory, (EntityWorkDroid)entity, inventory);
            }
        }

        TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));

        if (tileEntity == null)
            return null;

        if (ID == MALMain.GUI_ENUM.CHEST.ordinal())
            return new ContainerAutoChest(player.inventory, (TileEntityAutoChest)tileEntity);

        if (ID == MALMain.GUI_ENUM.FORESTER.ordinal())
            return new ContainerAutoForester();

        if (ID == MALMain.GUI_ENUM.FURNACE.ordinal())
            return new ContainerAutoFurnace(player.inventory, (TileEntityAutoFurnace)tileEntity);

        if (ID == MALMain.GUI_ENUM.QUARRY.ordinal())
            return new ContainerAutoQuarry();

        if (ID == MALMain.GUI_ENUM.WORKBENCH.ordinal())
            return new ContainerAutoWorkbench(player.inventory, (TileEntityAutoWorkbench)tileEntity);

        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player,
                                      World world, int x, int y, int z)
    {
        if (ID == MALMain.GUI_ENUM.DROID.ordinal())
        {
            Entity entity = world.getEntityByID(x);
            if (entity instanceof EntityWorkDroid)
            {
                InventoryEntityWorkDroid inventory = new InventoryEntityWorkDroid((EntityWorkDroid) entity);
                return new GuiEntityWorkDroid(player.inventory, (EntityWorkDroid)entity, inventory);
            }
        }

        TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));

        if (tileEntity == null)
            return null;

        if (ID == MALMain.GUI_ENUM.CHEST.ordinal())
            return new GuiAutoChest( player.inventory, (TileEntityAutoChest)tileEntity);

        if (ID == MALMain.GUI_ENUM.FORESTER.ordinal())
            return new GuiAutoForester( );

        if (ID == MALMain.GUI_ENUM.FURNACE.ordinal())
            return new GuiAutoFurnace( player.inventory, (TileEntityAutoFurnace)tileEntity);

        if (ID == MALMain.GUI_ENUM.QUARRY.ordinal())
            return new GuiAutoQuarry( );

        if (ID == MALMain.GUI_ENUM.WORKBENCH.ordinal())
            return new GuiAutoWorkbench( player.inventory, (TileEntityAutoWorkbench)tileEntity);


        return null;
    }
}
