package com.crashbox.vassal.chest;

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
 * <p>
 * The GUI for the beacon chest.  Pretty much identical to the normal chest.
 */
@SideOnly(Side.CLIENT)
public class GuiBeaconChest extends GuiContainer
{

    public GuiBeaconChest(InventoryPlayer parInventoryPlayer,
            IInventory parInventoryGrinder)
    {
        super(new ContainerBeaconChest(parInventoryPlayer,
                parInventoryGrinder));
        _inventoryPlayer = parInventoryPlayer;
        _tileBeacon = parInventoryGrinder;

        LOGGER.debug( "Constructed: " + this);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        String s = _tileBeacon.getDisplayName().getUnformattedText();
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
        mc.getTextureManager().bindTexture(guiTexture);
        int marginHorizontal = (width - xSize) / 2;
        int marginVertical = (height - ySize) / 2;
        drawTexturedModalRect(marginHorizontal, marginVertical, 0, 0, xSize, ySize);
    }


    @Override
    public String toString()
    {
        return "GuiBeaconChest{" +
                "inventoryPlayer=" + _inventoryPlayer +
                ", tileBeacon=" + _tileBeacon +
                '}';
    }

    private static final ResourceLocation guiTexture =
            new ResourceLocation(VassalMain.MODID +":textures/gui/container/beaconChest.png");

    private final InventoryPlayer _inventoryPlayer;
    private final IInventory _tileBeacon;

    private static final Logger LOGGER = LogManager.getLogger();
}
