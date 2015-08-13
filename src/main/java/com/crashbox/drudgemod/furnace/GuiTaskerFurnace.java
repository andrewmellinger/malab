package com.crashbox.drudgemod.furnace;

import com.crashbox.drudgemod.DrudgeMain;
import com.crashbox.drudgemod.furnace.ContainerTaskerFurnace;
import com.crashbox.drudgemod.furnace.TileEntityTaskerFurnace;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
@SideOnly(Side.CLIENT)
public class GuiTaskerFurnace extends GuiContainer
{
    private static final ResourceLocation grinderGuiTextures =
            new ResourceLocation(DrudgeMain.MODID
                    +":textures/gui/container/taskerFurnace.png");
    private final InventoryPlayer _inventoryPlayer;
    private final IInventory _tileTasker;

    public GuiTaskerFurnace(InventoryPlayer parInventoryPlayer,
            IInventory parInventoryGrinder)
    {
        super(new ContainerTaskerFurnace(parInventoryPlayer,
                parInventoryGrinder));
        _inventoryPlayer = parInventoryPlayer;
        _tileTasker = parInventoryGrinder;

        LOGGER.debug( "Constructed: " + this);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        String s = _tileTasker.getDisplayName().getUnformattedText();
        fontRendererObj.drawString(s, xSize/2-fontRendererObj
                .getStringWidth(s)/2, 6, 4210752);
        fontRendererObj.drawString(_inventoryPlayer.getDisplayName()
                .getUnformattedText(), 8, ySize - 96 + 2, 4210752);
    }

    /**
     * Args : renderPartialTicks, mouseX, mouseY
     */
    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks,
                                                   int mouseX, int mouseY)
    {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(grinderGuiTextures);
        int marginHorizontal = (width - xSize) / 2;
        int marginVertical = (height - ySize) / 2;
        drawTexturedModalRect(marginHorizontal, marginVertical, 0, 0,
                xSize, ySize);

        if(TileEntityTaskerFurnace.isBurning(_tileTasker))
        {
            int tmp = this.updateBurnIndicator(13);
            this.drawTexturedModalRect(marginHorizontal + 56, marginVertical + 36 + 12 - tmp, 176, 12 - tmp, 14, tmp + 1);
        }

        // Draw progress indicator
        int progressLevel = getProgressLevel(24);
        drawTexturedModalRect(marginHorizontal + 79, marginVertical + 34,
                176, 14, progressLevel + 1, 16);
    }

    private int getProgressLevel(int progressIndicatorPixelWidth)
    {
        int ticksGrindingItemSoFar = _tileTasker.getField(2);
        int ticksPerItem = _tileTasker.getField(3);
        return ticksPerItem != 0 && ticksGrindingItemSoFar != 0 ?
                ticksGrindingItemSoFar*progressIndicatorPixelWidth/ticksPerItem
                : 0;
    }

    private int updateBurnIndicator(int burnRemain) {
        int originalBurnTime = _tileTasker.getField(1);
        if(originalBurnTime == 0)
        {
            originalBurnTime = 200;
        }

        return _tileTasker.getField(0) * burnRemain / originalBurnTime;
    }

    @Override
    public String toString()
    {
        return "GuiTaskerFurnace{" +
                "_inventoryPlayer=" + _inventoryPlayer +
                ", _tileTasker=" + _tileTasker +
                '}';
    }

    private static final Logger LOGGER = LogManager.getLogger();
}
