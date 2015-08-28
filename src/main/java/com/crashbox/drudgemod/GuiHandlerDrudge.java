package com.crashbox.drudgemod;

import com.crashbox.drudgemod.DrudgeMain;
import com.crashbox.drudgemod.furnace.ContainerTaskerFurnace;
import com.crashbox.drudgemod.furnace.GuiTaskerFurnace;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
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

        return null;
    }

}
