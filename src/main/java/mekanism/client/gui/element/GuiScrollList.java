package mekanism.client.gui.element;

import java.util.ArrayList;
import java.util.List;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiScrollList extends GuiElement {

    private final int xPosition;
    private final int yPosition;
    private final int xSize;
    private final int size;

    private List<String> textEntries = new ArrayList<>();
    private boolean isDragging;
    private double dragOffset = 0;
    private int selected = -1;
    private double scroll;

    public GuiScrollList(IGuiWrapper gui, ResourceLocation def, int x, int y, int sizeX, int sizeY) {
        super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "GuiScrollList.png"), gui, def);

        xPosition = x;
        yPosition = y;

        xSize = sizeX;
        size = sizeY;
    }

    public boolean hasSelection() {
        return selected != -1;
    }

    public int getSelection() {
        return selected;
    }

    public void clearSelection() {
        this.selected = -1;
    }

    public void setText(List<String> text) {
        if (text == null) {
            textEntries.clear();
            return;
        }

        if (selected > text.size() - 1) {
            clearSelection();
        }

        textEntries = text;

        if (textEntries.size() <= size) {
            scroll = 0;
        }
    }

    @Override
    public Rectangle4i getBounds(int guiWidth, int guiHeight) {
        return new Rectangle4i(guiWidth + xPosition, guiHeight + yPosition, xSize, size * 10);
    }

    @Override
    public void renderBackground(int xAxis, int yAxis, int guiWidth, int guiHeight) {
        minecraft.textureManager.bindTexture(RESOURCE);
        drawBlack(guiWidth, guiHeight);
        drawSelected(guiWidth, guiHeight, selected);
        minecraft.textureManager.bindTexture(defaultLocation);
    }

    public void drawBlack(int guiWidth, int guiHeight) {
        int xDisplays = xSize / 10 + (xSize % 10 > 0 ? 1 : 0);

        for (int yIter = 0; yIter < size; yIter++) {
            for (int xIter = 0; xIter < xDisplays; xIter++) {
                int width = xSize % 10 > 0 && xIter == xDisplays - 1 ? xSize % 10 : 10;
                guiObj.drawTexturedRect(guiWidth + xPosition + (xIter * 10), guiHeight + yPosition + (yIter * 10), 0, 0, width, 10);
            }
        }
    }

    public void drawSelected(int guiWidth, int guiHeight, int index) {
        int scroll = getScrollIndex();

        if (selected != -1 && index >= scroll && index <= scroll + size - 1) {
            int xDisplays = xSize / 10 + (xSize % 10 > 0 ? 1 : 0);

            for (int xIter = 0; xIter < xDisplays; xIter++) {
                int width = xSize % 10 > 0 && xIter == xDisplays - 1 ? xSize % 10 : 10;
                guiObj.drawTexturedRect(guiWidth + xPosition + (xIter * 10), guiHeight + yPosition + (index - scroll) * 10, 0, 10, width, 10);
            }
        }
    }

    public void drawScroll() {
        int xStart = xPosition + xSize - 6;
        int yStart = yPosition;

        for (int i = 0; i < size; i++) {
            guiObj.drawTexturedRect(xStart, yStart + (i * 10), 10, 1, 6, 10);
        }

        guiObj.drawTexturedRect(xStart, yStart, 10, 0, 6, 1);
        guiObj.drawTexturedRect(xStart, yStart + (size * 10) - 1, 10, 0, 6, 1);

        guiObj.drawTexturedRect(xStart + 1, yStart + 1 + getScroll(), 16, 0, 4, 4);
    }

    public int getMaxScroll() {
        return (size * 10) - 2;
    }

    public int getScroll() {
        return Math.max(Math.min((int) (scroll * (getMaxScroll() - 4)), getMaxScroll() - 4), 0);
    }

    public int getScrollIndex() {
        if (textEntries.size() <= size) {
            return 0;
        }
        return (int) ((textEntries.size() * scroll) - ((float) size / (float) textEntries.size()) * scroll);
    }

    @Override
    public void renderForeground(int xAxis, int yAxis) {
        if (!textEntries.isEmpty()) {
            for (int i = 0; i < size; i++) {
                int index = getScrollIndex() + i;
                if (index <= textEntries.size() - 1) {
                    renderScaledText(textEntries.get(index), xPosition + 1, yPosition + 1 + (10 * i), 0x00CD00, xSize - 6);
                }
            }
        }

        minecraft.textureManager.bindTexture(RESOURCE);
        drawScroll();
        minecraft.textureManager.bindTexture(defaultLocation);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int xStart = xPosition + xSize - 5;

            if (mouseX >= xStart && mouseX <= xStart + 4 && mouseY >= getScroll() + yPosition + 1 && mouseY <= getScroll() + 4 + yPosition + 1) {
                if (textEntries.size() > size) {
                    dragOffset = mouseY - (getScroll() + yPosition + 1);
                    isDragging = true;
                    return true;
                }
            } else if (mouseX >= xPosition && mouseX <= xPosition + xSize - 6 && mouseY >= yPosition && mouseY <= yPosition + size * 10) {
                int index = getScrollIndex();
                clearSelection();
                for (int i = 0; i < size; i++) {
                    if (index + i <= textEntries.size() - 1) {
                        if (mouseY >= (yPosition + i * 10) && mouseY <= (yPosition + i * 10 + 10)) {
                            selected = index + i;
                            break;
                        }
                    }
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double mouseXOld, double mouseYOld) {
        //TODO: mouseXOld and mouseYOld are just guessed mappings I couldn't find any usage from a quick glance. look closer
        super.mouseDragged(mouseX, mouseY, button, mouseXOld, mouseYOld);
        if (isDragging) {
            scroll = Math.min(Math.max((mouseY - (yPosition + 1) - dragOffset) / (float) (getMaxScroll() - 4), 0), 1);
        }
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int type) {
        super.mouseReleased(mouseX, mouseY, type);
        if (type == 0) {
            if (isDragging) {
                dragOffset = 0;
                isDragging = false;
            }
        }
        return true;
    }

    @Override
    public void mouseWheel(int x, int y, int delta) {
        super.mouseWheel(x, y, delta);
        if (x > xPosition && x < xPosition + xSize && y > yPosition && y < yPosition + size * 10) {
            // 120 = DirectInput factor for one notch. Linux/OSX LWGL scale accordingly
            scroll = Math.min(Math.max(scroll - (delta / 120F) * (1F / textEntries.size()), 0), 1);
            drawScroll();
        }
    }
}