package com.crashbox.vassal.workbench;

import com.crashbox.vassal.VassalMain;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
@SideOnly(Side.CLIENT)
public class GuiBeaconWorkbench extends GuiContainer
{
    private static final ResourceLocation grinderGuiTextures =
            new ResourceLocation(VassalMain.MODID
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

        LOGGER.debug("Constructed: " + this);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
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
        drawTexturedModalRect(marginHorizontal, marginVertical, 0, 0, xSize, ySize);

        // Draw enabled thing
        if (_tileWorkbench.getEnabled())
        {
            drawTexturedModalRect(marginHorizontal + 120, marginVertical + 10,  // dst x, y
                    176, 16,                                                    // src x, y
                    16, 16);                                                    // width, height
        }

        // Draw progress indicator
        int progressLevel = (int) (_tileWorkbench.getProgressPercent() * 24F);
        drawTexturedModalRect(marginHorizontal + 117, marginVertical + 34,   // dst x, y
                176, 0,                                                      // src x, y
                progressLevel + 1, 16);                                      // width, height
    }


    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {

        super.mouseClicked(mouseX, mouseY, mouseButton);
        int marginHorizontal = (width - xSize) / 2;
        int marginVertical = (height - ySize) / 2;

        int x = mouseX - marginHorizontal;
        int y = mouseY - marginVertical;

        if (120 <= x && x <= 136 &&
              10 <= y && y <= 26 &&
                mouseButton == 0)
        {
            LOGGER.debug("Toggle enable");
            _tileWorkbench.toggleEnabled();
            // We need to send packes from client to server
        }
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
