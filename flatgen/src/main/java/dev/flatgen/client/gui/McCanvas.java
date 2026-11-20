package dev.flatgen.client.gui;

//? if <1.21 {
/*import com.mojang.blaze3d.systems.RenderSystem;
*///?}
import dev.flatgen.client.gui.rules.LayoutScale;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.mc.BlockSides;
import dev.flatgen.mc.Items;
//? if <1.21.11 {
import dev.flatgen.mc.MousePointer;
//?}
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
//? if >=1.21.11 {
/*import com.mojang.blaze3d.platform.cursor.CursorTypes;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.FontDescription;
*///?}
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

/**
 * A {@link Canvas} drawing through the game's GUI graphics, one frame at a time, in {@link Look}'s sprites and the
 * fonts of {@link TextStyle}. Up to 1.21.1 what is drawn over an item is lifted toward the player; from 1.21.11 on,
 * where the game draws items after the rest of a layer, it starts a layer of its own.
 */
final class McCanvas implements Canvas {
    /** How far toward the player what is drawn over items is lifted, as the game lifts an item's count (to 1.21.1). */
    private static final float OVER_ITEMS = 200;
    /** What ends text cut to fit. */
    private static final String ELLIPSIS = "\u2026";
    /** The shade over a control that cannot be used, ARGB: the face of a disabled button, see-through. */
    private static final int DIM = 0xA01B1D21;
    /** How far right of and above the mouse a tooltip is drawn, in GUI pixels. */
    private static final int TOOLTIP_OFFSET = 8;
    /** Space between a tooltip and what it is shown below, or the screen's edge, in GUI pixels. */
    private static final int TOOLTIP_GAP = 4;
    /** Space inside a tooltip's box around its words, in GUI pixels. */
    private static final int TOOLTIP_PADDING = 4;
    /** How far toward the player a tooltip is lifted, over everything else (to 1.21.1). */
    private static final float OVER_TOOLTIPS = 400;
    /** How far the focus ring stands out around what has the focus, in GUI pixels. */
    private static final int RING_SPREAD = 2;

    private final GuiGraphics graphics;
    private final Font font;
    private final BlockSides blockSides;
    /** The screen's own GUI pixels, whose window pixels pick the sharp fonts and which some calls convert. */
    private final LayoutScale scale;
    /** Whether an item was drawn in the layer drawn into now, which what goes over it leaves (from 1.21.11). */
    private boolean itemsInLayer;
    /** Whether the hand was asked for as the mouse's pointer this frame, which the frame's end shows (to 1.21.1). */
    private boolean handShown;
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
        ResourceLocation id = Objects.requireNonNull(ResourceLocation.tryParse(textStyle.font(pixelsPerUnit)));
        //? if >=1.21.11 {
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
        int y = style.top(area.y(), area.height());
        text(text, x, y, color, style);
    }

    @Override
    public Component fitted(Component text, int width, TextStyle style) {
        if (textWidth(text, style) <= width) {
            return text;
        }
        Component ellipsis = Component.literal(ELLIPSIS);
        String head = font.getSplitter().plainHeadByWidth(text.getString(),
                Math.max(0, width - textWidth(ellipsis, style)), style(style, scale.pixelsPerUnit()));
        return Component.literal(head.stripTrailing() + ELLIPSIS);
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
        //? if >=1.21.11 {
        /*graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, area.x(), area.y(), area.width(), area.height());
        *///?} else if >=1.21 {
        graphics.blitSprite(sprite, area.x(), area.y(), area.width(), area.height());
        //?} else
        /*GuiSprites.draw(graphics, sprite, area);*/
    }

    @Override
    public void blockSide(String block, int fluidColor, Rect area) {
        blockSides.draw(graphics, block, fluidColor, area.x(), area.y(), area.width(), area.height());
    }

    @Override
    public void blockIcon(String block, int x, int y) {
        blockSides.drawIcon(graphics, block, x, y);
        itemsInLayer = true;
    }

    @Override
    public void blockIcon(String block, Rect area) {
        float factor = area.width() / (float) Icon.SIZE;
        //? if >=1.21.11 {
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

    /** Draws in a stratum of its own above everything drawn so far, from 1.21.11; above the items before then. */
    @Override
    public void raised(Runnable drawing) {
        //? if >=1.21.11 {
        /*graphics.nextStratum();
        itemsInLayer = false;
        drawing.run();
        *///?} else {
        overItems(drawing);
        //?}
    }

    @Override
    public void showHand() {
        //? if >=1.21.11 {
        /*graphics.requestCursor(CursorTypes.POINTING_HAND);
        *///?} else
        handShown = true;
    }

    /** Draws above the items drawn so far. */
    private void overItems(Runnable draw) {
        //? if >=1.21.11 {
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

    /** A 16 by 16 texture at its own size, its clear parts clear, though 1.20.1's plain blit leaves blending be. */
    private void blitIcon(ResourceLocation texture, int x, int y) {
        //? if >=1.21.11 {
        /*graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0, 0, Icon.SIZE, Icon.SIZE, Icon.SIZE, Icon.SIZE);
        *///?} else if >=1.21 {
        graphics.blit(texture, x, y, 0, 0, Icon.SIZE, Icon.SIZE, Icon.SIZE, Icon.SIZE);
        //?} else {
        /*RenderSystem.enableBlend();
        graphics.blit(texture, x, y, 0, 0, Icon.SIZE, Icon.SIZE, Icon.SIZE, Icon.SIZE);
        RenderSystem.disableBlend();
        *///?}
    }

    /**
     * Clips to the area rounded outward to whole game GUI pixels, which a scissor is set in, so it never cuts a border
     * at the area's edge: up to 1.21.1 by converting it here, from 1.21.11, where the game converts it with the pose,
     * by widening it by a game pixel.
     */
    @Override
    public void clip(Rect area) {
        //? if >=1.21.11 {
        /*int slack = (int) Math.ceil(1 / scale.gameUnit());
        graphics.enableScissor(area.x() - slack, area.y() - slack, area.right() + slack, area.bottom() + slack);
        *///?} else {
        Rect game = scale.toGameOutward(area);
        graphics.enableScissor(game.x(), game.y(), game.right(), game.bottom());
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
     * Shows the mouse's pointer asked for (to 1.21.1; later the game shows the one asked for itself), then draws the
     * tooltip a widget asked for over everything else, in the look's dark box and body text, kept inside the screen.
     */
    void finish() {
        //? if <1.21.11 {
        MousePointer.hand(handShown);
        //?}
        if (tooltip.isEmpty()) {
            return;
        }
        int width = tooltip.stream().mapToInt(line -> textWidth(line, TextStyle.BODY)).max().orElse(0);
        int height = tooltip.size() * TextStyle.BODY.lineHeight();
        Rect box = inScreen(new Rect(tooltipX, tooltipY, width + 2 * TOOLTIP_PADDING, height + 2 * TOOLTIP_PADDING));
        //? if >=1.21.11 {
        /*graphics.nextStratum();
        drawTooltip(box);
        *///?} else {
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, OVER_TOOLTIPS);
        drawTooltip(box);
        graphics.pose().popPose();
        //?}
    }

    private void drawTooltip(Rect box) {
        sprite(Look.TOOLTIP, box);
        for (int line = 0; line < tooltip.size(); line++) {
            text(tooltip.get(line), box.x() + TOOLTIP_PADDING, box.y() + TOOLTIP_PADDING + 1
                    + line * TextStyle.BODY.lineHeight(), line == 0 ? Look.TEXT : Look.MUTED, TextStyle.BODY);
        }
    }

    /** Where a tooltip's box goes so it stays inside the screen: where it wants to be, moved in from the edges. */
    private Rect inScreen(Rect box) {
        int left = Math.max(TOOLTIP_GAP, Math.min(box.x(), scale.width() - box.width() - TOOLTIP_GAP));
        int top = Math.max(TOOLTIP_GAP, Math.min(box.y(), scale.height() - box.height() - TOOLTIP_GAP));
        return new Rect(left, top, box.width(), box.height());
    }
}
