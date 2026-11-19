package dev.flatgen.client.gui;

import dev.flatgen.client.gui.rules.LayoutScale;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.mc.BlockSides;
import dev.flatgen.mc.Items;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
//? if >=26.1 {
/*import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.FontDescription;
*///?}
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Vector2i;
import org.joml.Vector2ic;

/**
 * A {@link Canvas} drawing through the game's GUI graphics, one frame at a time, in {@link Look}'s sprites and the
 * fonts of {@link TextStyle}. Up to 1.21.x what is drawn over an item is lifted toward the player; from 26.1 on, where
 * the game draws items after the rest of a layer, it starts a layer of its own.
 */
final class McCanvas implements Canvas {
    /** How far toward the player what is drawn over items is lifted, as the game lifts an item's count (to 1.21.x). */
    private static final float OVER_ITEMS = 200;
    /** The shade over a control that cannot be used, ARGB: the face of a disabled button, see-through. */
    private static final int DIM = 0xA01B1D21;
    /** How far right of and above the mouse a tooltip is drawn, in GUI pixels. */
    private static final int TOOLTIP_OFFSET = 8;
    /** Space between a tooltip and what it is shown below, or the screen's edge, in GUI pixels. */
    private static final int TOOLTIP_GAP = 4;
    /** How far the focus ring stands out around what has the focus, in GUI pixels. */
    private static final int RING_SPREAD = 2;

    private final GuiGraphics graphics;
    private final Font font;
    private final BlockSides blockSides;
    /** The screen's own GUI pixels, whose window pixels pick the sharp fonts and which some calls convert. */
    private final LayoutScale scale;
    /** Whether an item was drawn in the layer drawn into now, which anything meant to be over it leaves (26.1). */
    private boolean itemsInLayer;
    private List<Component> tooltip = List.of();
    private int tooltipX;
    private int tooltipY;

    McCanvas(GuiGraphics graphics, Font font, BlockSides blockSides, LayoutScale scale) {
        this.graphics = graphics;
        this.font = font;
        this.blockSides = blockSides;
        this.scale = scale;
    }

    /** The style of text in a text style's font at this many window pixels per GUI pixel. */
    static Style style(TextStyle textStyle, int pixelsPerUnit) {
        ResourceLocation id = ResourceLocation.parse(textStyle.font(pixelsPerUnit));
        //? if >=26.1 {
        /*return Style.EMPTY.withFont(new FontDescription.Resource(id));
        *///?} else {
        return Style.EMPTY.withFont(id);
        //?}
    }

    /** Text drawn in a text style's font at this many window pixels per GUI pixel. */
    static Component styled(Component text, TextStyle textStyle, int pixelsPerUnit) {
        return Component.empty().append(text).withStyle(style(textStyle, pixelsPerUnit));
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
    public void text(Component text, int x, int y, int color, TextStyle style) {
        graphics.drawString(font, styled(text, style, scale.pixelsPerUnit()), x, y, color, false);
    }

    @Override
    public void centeredText(Component text, Rect area, int color, TextStyle style) {
        int x = area.x() + (area.width() - textWidth(text, style)) / 2;
        int y = area.y() + (area.height() - style.lineHeight()) / 2 + 1;
        text(text, x, y, color, style);
    }

    @Override
    public int textWidth(Component text, TextStyle style) {
        return font.width(styled(text, style, scale.pixelsPerUnit()));
    }

    @Override
    public void button(Rect area, boolean hovered, boolean usable) {
        sprite(Look.button(Look.ButtonKind.PLAIN, hovered, usable), area);
    }

    @Override
    public void field(Rect area, boolean lit) {
        sprite(lit ? Look.FIELD_LIT : Look.FIELD, area);
    }

    @Override
    public void sprite(String id, Rect area) {
        Optional.ofNullable(ResourceLocation.tryParse(id)).ifPresent(sprite -> sprite(sprite, area));
    }

    private void sprite(ResourceLocation sprite, Rect area) {
        //? if >=26.1 {
        /*graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, area.x(), area.y(), area.width(), area.height());
        *///?} else {
        graphics.blitSprite(sprite, area.x(), area.y(), area.width(), area.height());
        //?}
    }

    @Override
    public void blockSide(String block, int fluidColor, Rect area) {
        clip(area);
        blockSides.draw(graphics, block, fluidColor, area.x(), area.y(), area.width(), area.height());
        unclip();
    }

    @Override
    public void blockIcon(String block, int x, int y) {
        blockSides.drawIcon(graphics, block, x, y);
        itemsInLayer = true;
    }

    @Override
    public void blockIcon(String block, Rect area) {
        float factor = area.width() / (float) Icon.SIZE;
        //? if >=26.1 {
        /*graphics.pose().pushMatrix();
        graphics.pose().translate(area.x(), area.y());
        graphics.pose().scale(factor);
        blockIcon(block, 0, 0);
        graphics.pose().popMatrix();
        *///?} else {
        graphics.pose().pushPose();
        graphics.pose().translate(area.x(), area.y(), 0);
        graphics.pose().scale(factor, factor, 1);
        blockIcon(block, 0, 0);
        graphics.pose().popPose();
        //?}
    }

    @Override
    public void itemIcon(String item, int x, int y) {
        graphics.renderItem(Items.stack(item), x, y);
        itemsInLayer = true;
    }

    @Override
    public void sprite(String id, int x, int y) {
        Optional.ofNullable(ResourceLocation.tryParse(id))
                .ifPresent(sprite -> overItems(() -> sprite(sprite, new Rect(x, y, Icon.SIZE, Icon.SIZE))));
    }

    @Override
    public void markOver(String id, Rect area) {
        overItems(() -> sprite(id, area));
    }

    @Override
    public void count(Component number, Rect pill) {
        overItems(() -> drawCount(number, pill));
    }

    private void drawCount(Component number, Rect pill) {
        sprite(Look.COUNT, pill);
        centeredText(number, pill, Look.TEXT, TextStyle.STRONG);
    }

    @Override
    public void dim(Rect area) {
        overItems(() -> fill(area, DIM));
    }

    @Override
    public void focusRing(Rect area) {
        overItems(() -> drawRing(area));
    }

    private void drawRing(Rect area) {
        sprite(Look.FOCUS, new Rect(area.x() - RING_SPREAD, area.y() - RING_SPREAD, area.width() + 2 * RING_SPREAD,
                area.height() + 2 * RING_SPREAD));
    }

    /** Draws above the items drawn so far. */
    private void overItems(Runnable draw) {
        //? if >=26.1 {
        /*if (itemsInLayer) {
            graphics.nextStratum();
            itemsInLayer = false;
        }
        draw.run();
        *///?} else {
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, OVER_ITEMS);
        draw.run();
        graphics.pose().popPose();
        //?}
    }

    @Override
    public void texture(String location, int x, int y) {
        Optional.ofNullable(ResourceLocation.tryParse(location)).ifPresent(texture -> blitIcon(texture, x, y));
    }

    private void blitIcon(ResourceLocation texture, int x, int y) {
        //? if >=26.1 {
        /*graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0, 0, Icon.SIZE, Icon.SIZE, Icon.SIZE, Icon.SIZE);
        *///?} else {
        graphics.blit(texture, x, y, 0, 0, Icon.SIZE, Icon.SIZE, Icon.SIZE, Icon.SIZE);
        //?}
    }

    @Override
    public void clip(Rect area) {
        //? if >=26.1 {
        /*graphics.enableScissor(area.x(), area.y(), area.right(), area.bottom());
        *///?} else {
        graphics.enableScissor(scale.toGame(area.x()), scale.toGame(area.y()), scale.toGame(area.right()),
                scale.toGame(area.bottom()));
        //?}
    }

    @Override
    public void unclip() {
        graphics.disableScissor();
    }

    @Override
    public void tooltipBelow(List<Component> lines, Rect area) {
        tooltip = List.copyOf(lines);
        tooltipX = area.x() + TOOLTIP_GAP;
        tooltipY = area.bottom() + 2 * TOOLTIP_GAP;
    }

    @Override
    public void tooltip(List<Component> lines, int x, int y) {
        tooltip = List.copyOf(lines);
        tooltipX = x + TOOLTIP_OFFSET;
        tooltipY = y - TOOLTIP_OFFSET;
    }

    /**
     * Draws the tooltip a widget asked for over everything else, in the screen's own scale and its body text, kept
     * inside the screen.
     */
    void finish() {
        if (tooltip.isEmpty()) {
            return;
        }
        List<FormattedCharSequence> lines = tooltip.stream()
                .map(line -> styled(line, TextStyle.BODY, scale.pixelsPerUnit()).getVisualOrderText()).toList();
        ClientTooltipPositioner inScreen = (screenWidth, screenHeight, x, y, width, height) -> inScreen(x, y, width,
                height);
        //? if >=26.1 {
        /*graphics.nextStratum();
        graphics.tooltip(font, lines.stream().map(ClientTooltipComponent::create).toList(), tooltipX, tooltipY,
                inScreen, null);
        *///?} else {
        graphics.renderTooltip(font, lines, inScreen, tooltipX, tooltipY);
        //?}
    }

    /** Where a tooltip this big wanting its top left corner here goes so it stays inside the screen. */
    private Vector2ic inScreen(int x, int y, int width, int height) {
        int left = Math.max(TOOLTIP_GAP, Math.min(x, scale.width() - width - 2 * TOOLTIP_GAP));
        int top = Math.max(TOOLTIP_GAP, Math.min(y, scale.height() - height - 2 * TOOLTIP_GAP));
        return new Vector2i(left, top);
    }
}
