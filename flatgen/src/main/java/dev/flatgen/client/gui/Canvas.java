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

    /** Draws a button's face as the game draws its buttons: lit while hovered, greyed while it cannot be used. */
    void button(Rect area, boolean hovered, boolean usable);

    /** Draws a text field's box as the game draws its fields, lit while it has the keyboard. */
    void field(Rect area, boolean lit);

    /**
     * Fills a rectangle with a block's side, tiled at the size of a block in the inventory, as the block looks from
     * the side; a block drawn without faces of its own, such as water, is drawn tinted with the fluid colour given.
     */
    void blockSide(String block, int fluidColor, Rect area);

    /** Draws a block's item, 16 GUI pixels square, with its top left corner here; a block with no item, its side. */
    void blockIcon(String block, int x, int y);

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

    /** Shows a tooltip at a point once everything else is drawn. */
    void tooltip(List<Component> lines, int x, int y);
}
