package com.crashbox.mal.entity;

import com.crashbox.mal.VassalMain;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ResourceLocation;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
public class GuiEntityVassal extends GuiContainer
{
    private static final ResourceLocation grinderGuiTextures =
            new ResourceLocation(VassalMain.MODID
                    + ":textures/gui/container/vassal.png");
    private final InventoryPlayer _inventoryPlayer;
    private final IInventory _inventory;

    public GuiEntityVassal(InventoryPlayer parInventoryPlayer, EntityVassal vassal, InventoryEntityVassal inventory)
    {
        super(new ContainerEntityVassal(parInventoryPlayer, vassal, inventory));
        _vassal = vassal;
        _inventoryPlayer = parInventoryPlayer;
        _inventory = inventory;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        // Display name
        String s = _inventory.getDisplayName().getUnformattedText();
        fontRendererObj.drawString(s, xSize / 2 - fontRendererObj.getStringWidth(s) / 2, 6, 4210752);
        fontRendererObj.drawString(_inventoryPlayer.getDisplayName().getUnformattedText(), 8, ySize - 96 + 2, 4210752);

        // Text for held
        String heldTitle = new ChatComponentTranslation("container.vassal.title.held").getUnformattedText();
        fontRendererObj.drawString(heldTitle, 42, 20, 4210752);

        // Text for fuel
        String fuelTitle = new ChatComponentTranslation("container.vassal.title.fuel").getUnformattedText();
        fuelTitle += ": " + _vassal.getFuelSecs();
        fontRendererObj.drawString(fuelTitle, 42, 56, 4210752);

        // Text for follow me
        String followMeTitle = new ChatComponentTranslation("container.vassal.title.followme").getUnformattedText();
        int start = 141 - fontRendererObj.getStringWidth(followMeTitle);
        fontRendererObj.drawString(followMeTitle, start, 20, 4210752);
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

    private final EntityVassal _vassal;
}

