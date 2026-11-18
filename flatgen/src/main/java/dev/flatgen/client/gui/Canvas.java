package dev.flatgen.client.gui;

import dev.flatgen.client.gui.rules.Rect;
import java.util.List;
import net.minecraft.network.chat.Component;

/**
 * What Flatgen's widgets draw with, in GUI pixels. Widgets never touch the game's drawing classes, which change between
 * versions; each version's {@link McCanvas} draws these through them (docs/plan.md 3.3). Colours are ARGB.
 */
public interface Canvas {
    /** Fills a rectangle with a colour. */
    void fill(Rect area, int color);

    /** Draws a one-pixel line around the inside of a rectangle. */
    void outline(Rect area, int color);

    /** Draws text with its top left corner here, with the game's shadow. */
    void text(Component text, int x, int y, int color);

    /** Draws text in the middle of a rectangle. */
    void centeredText(Component text, Rect area, int color);

    /** How wide text is drawn. */
    int textWidth(Component text);

    /** How tall a line of text is. */
    int lineHeight();

    /** Draws one of the game's GUI sprites stretched over a rectangle, as {@code minecraft:widget/button}. */
    void sprite(String sprite, Rect area);

    /**
     * Fills a rectangle with a block's side, tiled at the size of a block in the inventory, as the block looks from
     * the side; a block drawn without faces of its own, such as water, is drawn tinted with the fluid colour given.
     */
    void blockSide(String block, int fluidColor, Rect area);

    /** Keeps what is drawn next inside a rectangle until {@link #unclip()}; clips nest. */
    void clip(Rect area);

    /** Ends the innermost {@link #clip}. */
    void unclip();

    /** Shows a tooltip at a point once everything else is drawn. */
    void tooltip(List<Component> lines, int x, int y);
}
