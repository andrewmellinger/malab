package com.crashbox.drudgemod.workbench;

import com.crashbox.drudgemod.DrudgeMain;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
@SideOnly(Side.CLIENT)
public class GuiBeaconWorkbench extends GuiContainer
{
    private static final ResourceLocation grinderGuiTextures =
            new ResourceLocation(DrudgeMain.MODID
                +":textures/gui/container/beaconWorkbench.png");
    private final InventoryPlayer _inventoryPlayer;
    private final TileEntityBeaconWorkbench _tileWorkbench;

    public GuiBeaconWorkbench(InventoryPlayer parInventoryPlayer,
            TileEntityBeaconWorkbench workbench)
    {
        // We need to set yp our own craft matrix.
        super(new ContainerBeaconWorkbench(parInventoryPlayer, workbench));

        _inventoryPlayer = parInventoryPlayer;
        _tileWorkbench = workbench;

        LOGGER.debug( "Constructed: " + this);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        // TODO: What is all this?
//        LOGGER.debug("drawGuiContainerForegroundLayer");
//        String s = _tileWorkbench.getDisplayName().getUnformattedText();
        String s = _tileWorkbench.getDisplayName().getUnformattedText();
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
//        LOGGER.debug("drawGuiContainerBackgroundLayer");
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(grinderGuiTextures);
        int marginHorizontal = (width - xSize) / 2;
        int marginVertical = (height - ySize) / 2;
        drawTexturedModalRect(marginHorizontal, marginVertical, 0, 0,
                xSize, ySize);

//        if(TileEntityBeaconWorkbench.isBurning(_tileWorkbench))
//        {
//            int tmp = this.updateBurnIndicator(13);
//            this.drawTexturedModalRect(marginHorizontal + 56, marginVertical + 36 + 12 - tmp, 176, 12 - tmp, 14, tmp + 1);
//        }

//        // Draw progress indicator
//        int progressLevel = getProgressLevel(24);
//        drawTexturedModalRect(marginHorizontal + 79, marginVertical + 34,
//                176, 14, progressLevel + 1, 16);
    }

    private int getProgressLevel(int progressIndicatorPixelWidth)
    {
        int totalSoFar = _tileWorkbench._accumulatedItemSmeltTicks;
        int ticksPerItem = _tileWorkbench._totalItemSmeltTicks;
        return ticksPerItem != 0 && totalSoFar != 0 ?
                totalSoFar*progressIndicatorPixelWidth/ticksPerItem
                : 0;
    }

    private int updateBurnIndicator(int burnRemain) {
        int originalBurnTime = _tileWorkbench._originalFuelBurnTicks;
        if(originalBurnTime == 0)
        {
            originalBurnTime = 200;
        }

        return _tileWorkbench._remainingFuelBurnTicks * burnRemain / originalBurnTime;
    }

    @Override
    public String toString()
    {
        return "GuiBeaconWorkbench{" +
                "_inventoryPlayer=" + _inventoryPlayer +
                ", _tileWorkbench=" + _tileWorkbench +
                '}';
    }

    private static final Logger LOGGER = LogManager.getLogger();
}
