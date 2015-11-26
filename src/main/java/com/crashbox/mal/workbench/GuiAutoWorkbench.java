package com.crashbox.mal.workbench;

import com.crashbox.mal.MALMain;
import com.crashbox.mal.network.MessageToggleWorkbenchEnable;
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
public class GuiAutoWorkbench extends GuiContainer
{
    private static final ResourceLocation guiTextures =
            new ResourceLocation(MALMain.MODID
                +":textures/gui/container/workbench.png");
    private final InventoryPlayer _inventoryPlayer;
    private final TileEntityAutoWorkbench _tileWorkbench;

    public GuiAutoWorkbench(InventoryPlayer parInventoryPlayer,
                            TileEntityAutoWorkbench workbench)
    {
        super(new ContainerAutoWorkbench(parInventoryPlayer, workbench));
        _inventoryPlayer = parInventoryPlayer;
        _tileWorkbench = workbench;
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
        mc.getTextureManager().bindTexture(guiTextures);
        int marginHorizontal = (width - xSize) / 2;
        int marginVertical = (height - ySize) / 2;
        drawTexturedModalRect(marginHorizontal, marginVertical, 0, 0, xSize, ySize);

        // Draw enabled thing
//        if (_tileWorkbench.getEnabled())
//        {
//            drawTexturedModalRect(marginHorizontal + 120, marginVertical + 16,  // dst x, y
//                    176, 16,                                                    // src x, y
//                    16, 16);                                                    // width, height
//        }

        // Draw progress indicator
        int progressLevel = (int) (_tileWorkbench.getProgressPercent() * 24F);
        drawTexturedModalRect(marginHorizontal + 117, marginVertical + 44,   // dst x, y
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
              16 <= y && y <= 32 &&
                mouseButton == 0)
        {
            // Send a packet to the server toggling enabled
            MessageToggleWorkbenchEnable enable = new MessageToggleWorkbenchEnable();
            enable.setWorldID(_tileWorkbench.getWorld().provider.getDimensionId());
            enable.setPos(_tileWorkbench.getPos());
        }
    }

    @Override
    public String toString()
    {
        return "GuiAutoWorkbench{" +
                "_inventoryPlayer=" + _inventoryPlayer +
                ", _tileWorkbench=" + _tileWorkbench +
                '}';
    }

    private static final Logger LOGGER = LogManager.getLogger();
}
