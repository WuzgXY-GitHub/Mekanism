package mekanism.client.gui.element.tab;

import mekanism.api.TileNetworkList;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiTileEntityElement;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tile.factory.TileEntityFactory;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiSortingTab extends GuiTileEntityElement<TileEntityFactory> {

    public GuiSortingTab(IGuiWrapper gui, TileEntityFactory tile, ResourceLocation def) {
        super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "GuiSortingTab.png"), gui, def, tile);
    }

    @Override
    public Rectangle4i getBounds(int guiWidth, int guiHeight) {
        return new Rectangle4i(guiWidth - 26, guiHeight + 62, 26, 35);
    }

    @Override
    protected boolean inBounds(int xAxis, int yAxis) {
        return xAxis >= -21 && xAxis <= -3 && yAxis >= 66 && yAxis <= 84;
    }

    @Override
    public void renderBackground(int xAxis, int yAxis, int guiWidth, int guiHeight) {
        minecraft.textureManager.bindTexture(RESOURCE);
        guiObj.drawTexturedRect(guiWidth - 26, guiHeight + 62, 0, 0, 26, 35);
        guiObj.drawTexturedRect(guiWidth - 21, guiHeight + 66, 26, inBounds(xAxis, yAxis) ? 0 : 18, 18, 18);
        minecraft.textureManager.bindTexture(defaultLocation);
    }

    @Override
    public void renderForeground(int xAxis, int yAxis) {
        minecraft.textureManager.bindTexture(RESOURCE);
        getFontRenderer().drawString(LangUtils.transOnOff(tileEntity.sorting), -21, 86, 0x0404040);
        if (inBounds(xAxis, yAxis)) {
            displayTooltip(LangUtils.localize("gui.factory.autoSort"), xAxis, yAxis);
        }
        minecraft.textureManager.bindTexture(defaultLocation);
        MekanismRenderer.resetColor();
    }

    @Override
    public void preMouseClicked(int xAxis, int yAxis, int button) {
    }

    @Override
    public void mouseClicked(int xAxis, int yAxis, int button) {
        if (button == 0 && inBounds(xAxis, yAxis)) {
            TileNetworkList data = TileNetworkList.withContents(0);
            Mekanism.packetHandler.sendToServer(new PacketTileEntity(tileEntity, data));
            SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
        }
    }
}