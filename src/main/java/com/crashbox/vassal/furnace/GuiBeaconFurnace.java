package com.crashbox.vassal.furnace;

import com.crashbox.vassal.VassalMain;
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
public class GuiBeaconFurnace extends GuiContainer
{
    private static final ResourceLocation grinderGuiTextures =
            new ResourceLocation(VassalMain.MODID
                    +":textures/gui/container/beaconFurnace.png");
    private final InventoryPlayer _inventoryPlayer;
    private final IInventory _tileBeacon;

    public GuiBeaconFurnace(InventoryPlayer parInventoryPlayer,
            IInventory parInventoryGrinder)
    {
        super(new ContainerBeaconFurnace(parInventoryPlayer,
                parInventoryGrinder));
        _inventoryPlayer = parInventoryPlayer;
        _tileBeacon = parInventoryGrinder;

        LOGGER.debug( "Constructed: " + this);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        String s = _tileBeacon.getDisplayName().getUnformattedText();
        fontRendererObj.drawString(s, xSize/2-fontRendererObj.getStringWidth(s)/2, 6, 4210752);
        fontRendererObj.drawString(_inventoryPlayer.getDisplayName().getUnformattedText(), 8, ySize - 96 + 2, 4210752);
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
        drawTexturedModalRect(marginHorizontal, marginVertical, 0, 0, xSize, ySize);

        // Draw burning icon
        if(TileEntityBeaconFurnace.isBurning(_tileBeacon))
        {
            int tmp = this.updateBurnIndicator(13);
//            this.drawTexturedModalRect(marginHorizontal + 56, marginVertical + 36 + 12 - tmp, 176, 12 - tmp, 14, tmp + 1);
            this.drawTexturedModalRect(marginHorizontal + 88, marginVertical + 36 + 12 - tmp, 176, 12 - tmp, 14, tmp + 1);
        }

        // Draw progress indicator
        int progressLevel = getProgressLevel(24);
//        drawTexturedModalRect(marginHorizontal + 79, marginVertical + 34, 176, 14, progressLevel + 1, 16);
        drawTexturedModalRect(marginHorizontal + 111, marginVertical + 34, 176, 14, progressLevel + 1, 16);
    }

    private int getProgressLevel(int progressIndicatorPixelWidth)
    {
        int ticksGrindingItemSoFar = _tileBeacon.getField(2);
        int ticksPerItem = _tileBeacon.getField(3);
        return ticksPerItem != 0 && ticksGrindingItemSoFar != 0 ?
                ticksGrindingItemSoFar*progressIndicatorPixelWidth/ticksPerItem
                : 0;
    }

    private int updateBurnIndicator(int burnRemain) {
        int originalBurnTime = _tileBeacon.getField(1);
        if(originalBurnTime == 0)
        {
            originalBurnTime = 200;
        }

        return _tileBeacon.getField(0) * burnRemain / originalBurnTime;
    }

    @Override
    public String toString()
    {
        return "GuiBeaconFurnace{" +
                "_inventoryPlayer=" + _inventoryPlayer +
                ", _tileBeacon=" + _tileBeacon +
                '}';
    }

    private static final Logger LOGGER = LogManager.getLogger();
}
