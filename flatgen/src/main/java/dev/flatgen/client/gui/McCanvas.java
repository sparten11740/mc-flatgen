package dev.flatgen.client.gui;

import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.mc.BlockSides;
import dev.flatgen.mc.Items;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** A {@link Canvas} drawing through this version's {@link GuiGraphics}, one frame at a time. */
final class McCanvas implements Canvas {
    private static final ResourceLocation BUTTON = ResourceLocation.withDefaultNamespace("widget/button");
    private static final ResourceLocation LIT_BUTTON =
            ResourceLocation.withDefaultNamespace("widget/button_highlighted");
    private static final ResourceLocation GREY_BUTTON = ResourceLocation.withDefaultNamespace("widget/button_disabled");
    private static final ResourceLocation FIELD = ResourceLocation.withDefaultNamespace("widget/text_field");
    private static final ResourceLocation LIT_FIELD =
            ResourceLocation.withDefaultNamespace("widget/text_field_highlighted");
    /** How far toward the player what is drawn over items is lifted, as the game lifts an item's count. */
    private static final float OVER_ITEMS = 200;
    /** The shade laid over a control that cannot be used, ARGB. */
    private static final int DIM = 0x90000000;
    /** How far the game draws a tooltip right of and above the point it is shown at, in GUI pixels. */
    private static final int TOOLTIP_OFFSET = 12;
    /** Space between a rectangle and a tooltip shown below it, in GUI pixels. */
    private static final int TOOLTIP_GAP = 4;
    /** The focus ring's colours, outside and inside. */
    private static final int RING_OUTSIDE = 0xFFFFFFFF;
    private static final int RING_INSIDE = 0xFF000000;

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
    public void button(Rect area, boolean hovered, boolean usable) {
        sprite(!usable ? GREY_BUTTON : hovered ? LIT_BUTTON : BUTTON, area);
    }

    @Override
    public void field(Rect area, boolean lit) {
        sprite(lit ? LIT_FIELD : FIELD, area);
    }

    private void sprite(ResourceLocation sprite, Rect area) {
        graphics.blitSprite(sprite, area.x(), area.y(), area.width(), area.height());
    }

    @Override
    public void blockSide(String block, int fluidColor, Rect area) {
        blockSides.draw(graphics, block, fluidColor, area.x(), area.y(), area.width(), area.height());
    }

    @Override
    public void blockIcon(String block, int x, int y) {
        blockSides.drawIcon(graphics, block, x, y);
    }

    @Override
    public void itemIcon(String item, int x, int y) {
        graphics.renderItem(Items.stack(item), x, y);
    }

    @Override
    public void sprite(String id, int x, int y) {
        Optional.ofNullable(ResourceLocation.tryParse(id)).ifPresent(sprite -> overItems(() ->
                graphics.blitSprite(sprite, x, y, Icon.SIZE, Icon.SIZE)));
    }

    @Override
    public void dim(Rect area) {
        overItems(() -> graphics.fill(area.x(), area.y(), area.right(), area.bottom(), DIM));
    }

    @Override
    public void focusRing(Rect area) {
        overItems(() -> drawRing(area));
    }

    private void drawRing(Rect area) {
        outline(new Rect(area.x() - 1, area.y() - 1, area.width() + 2, area.height() + 2), RING_OUTSIDE);
        outline(area, RING_INSIDE);
    }

    /** Draws above the items drawn so far. */
    private void overItems(Runnable draw) {
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, OVER_ITEMS);
        draw.run();
        graphics.pose().popPose();
    }

    @Override
    public void texture(String location, int x, int y) {
        Optional.ofNullable(ResourceLocation.tryParse(location)).ifPresent(texture ->
                graphics.blit(texture, x, y, 0, 0, Icon.SIZE, Icon.SIZE, Icon.SIZE, Icon.SIZE));
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
    public void tooltipBelow(List<Component> lines, Rect area) {
        tooltip(lines, area.x() - TOOLTIP_OFFSET, area.bottom() + TOOLTIP_GAP + TOOLTIP_OFFSET);
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
