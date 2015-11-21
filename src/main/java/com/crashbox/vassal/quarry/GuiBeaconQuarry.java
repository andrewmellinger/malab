package com.crashbox.vassal.quarry;

import com.crashbox.vassal.VassalMain;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Copyright 2015 Andrew O. Mellinger
 */
@SideOnly(Side.CLIENT)
public class GuiBeaconQuarry extends GuiContainer
{
    public GuiBeaconQuarry()
    {
        super(new ContainerBeaconQuarry());
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        String s = new ChatComponentTranslation("container.beaconQuarry.directions").getUnformattedText();
        fontRendererObj.drawSplitString(s, 6, 6, xSize - 12, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(guiTexture);
        int marginHorizontal = (width - xSize) / 2;
        int marginVertical = (height - ySize) / 2;
        drawTexturedModalRect(marginHorizontal, marginVertical, 0, 0, xSize, ySize);
    }

    private static final ResourceLocation guiTexture = new ResourceLocation(VassalMain.MODID
                    + ":textures/gui/container/beaconQuarry.png");
}
