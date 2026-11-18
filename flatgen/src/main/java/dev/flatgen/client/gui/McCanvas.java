package dev.flatgen.client.gui;

import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.mc.BlockSides;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** A {@link Canvas} drawing through this version's {@link GuiGraphics}, one frame at a time. */
final class McCanvas implements Canvas {
    private final GuiGraphics graphics;
    private final Font font;
    private final BlockSides blockSides;
    private List<Component> tooltip = List.of();
    private int tooltipX;
    private int tooltipY;

    McCanvas(GuiGraphics graphics, Font font, BlockSides blockSides) {
        this.graphics = graphics;
        this.font = font;
        this.blockSides = blockSides;
    }

    @Override
    public void fill(Rect area, int color) {
        graphics.fill(area.x(), area.y(), area.right(), area.bottom(), color);
    }

    @Override
    public void outline(Rect area, int color) {
        graphics.renderOutline(area.x(), area.y(), area.width(), area.height(), color);
    }

    @Override
    public void text(Component text, int x, int y, int color) {
        graphics.drawString(font, text, x, y, color);
    }

    @Override
    public void centeredText(Component text, Rect area, int color) {
        int x = area.x() + (area.width() - font.width(text)) / 2;
        int y = area.y() + (area.height() - font.lineHeight) / 2 + 1;
        graphics.drawString(font, text, x, y, color);
    }

    @Override
    public int textWidth(Component text) {
        return font.width(text);
    }

    @Override
    public int lineHeight() {
        return font.lineHeight;
    }

    @Override
    public void sprite(String sprite, Rect area) {
        graphics.blitSprite(ResourceLocation.parse(sprite), area.x(), area.y(), area.width(), area.height());
    }

    @Override
    public void blockSide(String block, int fluidColor, Rect area) {
        blockSides.draw(graphics, block, fluidColor, area.x(), area.y(), area.width(), area.height());
    }

    @Override
    public void clip(Rect area) {
        graphics.enableScissor(area.x(), area.y(), area.right(), area.bottom());
    }

    @Override
    public void unclip() {
        graphics.disableScissor();
    }

    @Override
    public void tooltip(List<Component> lines, int x, int y) {
        tooltip = List.copyOf(lines);
        tooltipX = x;
        tooltipY = y;
    }

    /** Draws the tooltip a widget asked for, over everything else. */
    void finish() {
        if (!tooltip.isEmpty()) {
            graphics.renderTooltip(font, tooltip, Optional.empty(), tooltipX, tooltipY);
        }
    }
}
