package com.crashbox.vassal;

import com.crashbox.vassal.chest.ContainerBeaconChest;
import com.crashbox.vassal.chest.GuiBeaconChest;
import com.crashbox.vassal.chest.TileEntityBeaconChest;
import com.crashbox.vassal.entity.ContainerEntityVassal;
import com.crashbox.vassal.entity.EntityVassal;
import com.crashbox.vassal.entity.GuiEntityVassal;
import com.crashbox.vassal.entity.InventoryEntityVassal;
import com.crashbox.vassal.furnace.ContainerBeaconFurnace;
import com.crashbox.vassal.furnace.GuiBeaconFurnace;
import com.crashbox.vassal.furnace.TileEntityBeaconFurnace;
import com.crashbox.vassal.workbench.ContainerBeaconWorkbench;
import com.crashbox.vassal.workbench.GuiBeaconWorkbench;
import com.crashbox.vassal.workbench.TileEntityBeaconWorkbench;
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
public class GuiHandlerVassal implements IGuiHandler
{
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player,
                                      World world, int x, int y, int z)
    {
        if (ID == VassalMain.GUI_ENUM.VASSAL.ordinal())
        {
            Entity entity = world.getEntityByID(x);
            if (entity instanceof EntityVassal)
            {
                InventoryEntityVassal inventory = new InventoryEntityVassal((EntityVassal) entity);
                return new ContainerEntityVassal(player.inventory, (EntityVassal)entity, inventory);
            }
        }

        TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));

        if (tileEntity == null)
            return null;

        if (ID == VassalMain.GUI_ENUM.FURNACE.ordinal())
            return new ContainerBeaconFurnace(player.inventory, (TileEntityBeaconFurnace)tileEntity);

        if (ID == VassalMain.GUI_ENUM.WORKBENCH.ordinal())
            return new ContainerBeaconWorkbench(player.inventory, (TileEntityBeaconWorkbench)tileEntity);

        if (ID == VassalMain.GUI_ENUM.CHEST.ordinal())
            return new ContainerBeaconChest(player.inventory, (TileEntityBeaconChest)tileEntity);

        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player,
                                      World world, int x, int y, int z)
    {
        if (ID == VassalMain.GUI_ENUM.VASSAL.ordinal())
        {
            Entity entity = world.getEntityByID(x);
            if (entity instanceof EntityVassal)
            {
                InventoryEntityVassal inventory = new InventoryEntityVassal((EntityVassal) entity);
                return new GuiEntityVassal(player.inventory, (EntityVassal)entity, inventory);
            }
        }

        TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));

        if (tileEntity == null)
            return null;

        if (ID == VassalMain.GUI_ENUM.FURNACE.ordinal())
            return new GuiBeaconFurnace( player.inventory, (TileEntityBeaconFurnace)tileEntity);

        if (ID == VassalMain.GUI_ENUM.WORKBENCH.ordinal())
            return new GuiBeaconWorkbench( player.inventory, (TileEntityBeaconWorkbench)tileEntity);

        if (ID == VassalMain.GUI_ENUM.CHEST.ordinal())
            return new GuiBeaconChest( player.inventory, (TileEntityBeaconChest)tileEntity);

        return null;
    }
}
