package dev.flatgen.client.gui;

import dev.flatgen.client.gui.rules.Rect;
import java.util.List;
import net.minecraft.network.chat.Component;

/**
 * What Flatgen's widgets draw with, in GUI pixels, which on Flatgen's screens are their own ({@link
 * dev.flatgen.client.gui.rules.LayoutScale}). Widgets never touch the game's drawing classes, which change between
 * versions; each version's {@link McCanvas} draws these through them (docs/plan.md 3.3). Colours are ARGB.
 */
public interface Canvas {
    /** Fills a rectangle with a colour. */
    void fill(Rect area, int color);

    /** Draws a one-pixel line around the inside of a rectangle. */
    void outline(Rect area, int color);

    /** Draws text in a style with its top left corner here. */
    void text(Component text, int x, int y, int color, TextStyle style);

    /** Draws body text with its top left corner here. */
    default void text(Component text, int x, int y, int color) {
        text(text, x, y, color, TextStyle.BODY);
    }

    /** Draws text in a style in the middle of a rectangle. */
    void centeredText(Component text, Rect area, int color, TextStyle style);

    /** Draws body text in the middle of a rectangle. */
    default void centeredText(Component text, Rect area, int color) {
        centeredText(text, area, color, TextStyle.BODY);
    }

    /** How wide text is drawn in a style. */
    int textWidth(Component text, TextStyle style);

    /** How wide body text is drawn. */
    default int textWidth(Component text) {
        return textWidth(text, TextStyle.BODY);
    }

    /** How tall a line of body text is. */
    default int lineHeight() {
        return TextStyle.BODY.lineHeight();
    }

    /** Draws a plain button's face: lit while hovered, greyed while it cannot be used. */
    void button(Rect area, boolean hovered, boolean usable);

    /** Draws a text field's box, lit while it has the keyboard. */
    void field(Rect area, boolean lit);

    /**
     * Draws a sprite of the game's GUI atlas by its id, such as one of {@link Look}'s, over a rectangle: stretched, or
     * with its corners kept, as the sprite's own settings say.
     */
    void sprite(String id, Rect area);

    /**
     * Fills a rectangle with a block's side, tiled at the size of a block in the inventory, as the block looks from
     * the side; a block drawn without faces of its own, such as water, is drawn tinted with the fluid colour given.
     */
    void blockSide(String block, int fluidColor, Rect area);

    /** Draws a block's item, 16 GUI pixels square, with its top left corner here; a block with no item, its side. */
    void blockIcon(String block, int x, int y);

    /** Draws a sprite over what is drawn there, items too, such as a mark on a block's picture. */
    void markOver(String id, Rect area);

    /** Draws a number on a dark pill over what is drawn there, items too, as the game draws a stack's count. */
    void count(Component number, Rect pill);

    /** Draws a block's item, or its side when it has none, filling a square larger or smaller than an icon. */
    void blockIcon(String block, Rect area);

    /** Draws an item, 16 GUI pixels square, with its top left corner here; nothing for an item the game lacks. */
    void itemIcon(String item, int x, int y);

    /**
     * Draws a sprite of the game's GUI atlas 16 GUI pixels square, by its id, such as {@code flatgen:icon/undo}, with
     * its top left corner here, above an item drawn there, as an item's count is.
     */
    void sprite(String id, int x, int y);

    /**
     * Draws the ring around the control that has the keyboard's focus, white outside and black inside, so it shows on
     * any background by its shape; above items.
     */
    void focusRing(Rect area);

    /** Darkens what is drawn in a rectangle, an item there too, as a control that cannot be used is drawn. */
    void dim(Rect area);

    /** Draws a texture 16 by 16 pixels, by its location, with its top left corner here; nothing for a bad location. */
    void texture(String location, int x, int y);

    /** Keeps what is drawn next inside a rectangle until {@link #unclip()}; clips nest. */
    void clip(Rect area);

    /** Ends the innermost {@link #clip}. */
    void unclip();

    /** Shows a tooltip just below a rectangle, its left edges lined up, once everything else is drawn. */
    void tooltipBelow(List<Component> lines, Rect area);

    /** Shows a tooltip at a point once everything else is drawn. */
    void tooltip(List<Component> lines, int x, int y);
}
