package com.crashbox.drudgemod;

import com.crashbox.drudgemod.furnace.ContainerTaskerFurnace;
import com.crashbox.drudgemod.furnace.GuiTaskerFurnace;
import com.crashbox.drudgemod.workbench.ContainerTaskerWorkbench;
import com.crashbox.drudgemod.workbench.GuiTaskerWorkbench;
import com.crashbox.drudgemod.workbench.TileEntityTaskerWorkbench;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
@SideOnly(Side.CLIENT)
public class GuiHandlerDrudge implements IGuiHandler
{
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player,
                                      World world, int x, int y, int z)
    {
        TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));

        if (tileEntity == null)
            return null;

        if (ID == DrudgeMain.GUI_ENUM.FURNACE.ordinal())
            return new ContainerTaskerFurnace(player.inventory, (IInventory)tileEntity);

        if (ID == DrudgeMain.GUI_ENUM.WORKBENCH.ordinal())
            return new ContainerTaskerWorkbench(player.inventory, (TileEntityTaskerWorkbench)tileEntity);

        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player,
                                      World world, int x, int y, int z)
    {
        TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));

        if (tileEntity == null)
            return null;

        if (ID == DrudgeMain.GUI_ENUM.FURNACE.ordinal())
            return new GuiTaskerFurnace( player.inventory, (IInventory)tileEntity);

        if (ID == DrudgeMain.GUI_ENUM.WORKBENCH.ordinal())
            return new GuiTaskerWorkbench( player.inventory, (TileEntityTaskerWorkbench)tileEntity);

        return null;
    }


    private static final Logger LOGGER = LogManager.getLogger();

}
