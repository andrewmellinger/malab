package com.crashbox.vassal.entity;

import com.crashbox.vassal.VassalMain;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class GuiEntityVassal extends GuiContainer
{
    private static final ResourceLocation grinderGuiTextures =
            new ResourceLocation(VassalMain.MODID
                    +":textures/gui/container/vassal.png");
    private final InventoryPlayer _inventoryPlayer;
    private final IInventory _inventory;

    public GuiEntityVassal(InventoryPlayer parInventoryPlayer, EntityVassal vassal, InventoryEntityVassal inventory)
    {
        super(new ContainerEntityVassal(parInventoryPlayer, vassal, inventory));
        _inventoryPlayer = parInventoryPlayer;
        _inventory = inventory;

        LOGGER.debug( "Constructed: " + this);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        String s = _inventory.getDisplayName().getUnformattedText();
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
    }

    @Override
    public String toString()
    {
        return "GuiEntityVassal{" +
                "_inventoryPlayer=" + _inventoryPlayer +
                ", _inventory=" + _inventory +
                '}';
    }

    private static final Logger LOGGER = LogManager.getLogger();}
