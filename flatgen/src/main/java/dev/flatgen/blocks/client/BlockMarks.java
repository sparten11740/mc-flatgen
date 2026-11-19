package dev.flatgen.blocks.client;

import dev.flatgen.client.gui.Canvas;
import dev.flatgen.client.gui.Look;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.mc.BlockCatalog;

/**
 * The marks on a block's picture that say what it will do in a stack without words: a block that falls with nothing
 * under it wears a down arrow on a yellow pill at its top right corner, alike in the picker and on the layer cards.
 */
public final class BlockMarks {
    /** The mark's size, and the picture inside its pill, in GUI pixels. */
    private static final int MARK = 10;
    private static final int PICTURE = 8;

    private BlockMarks() {
    }

    /** Draws a block's marks at the top right corner of its picture, over it. */
    public static void draw(Canvas canvas, String block, Rect picture) {
        if (!BlockCatalog.traits(block).falls()) {
            return;
        }
        Rect mark = new Rect(picture.right() - MARK / 2, picture.y() - MARK / 2, MARK, MARK);
        canvas.markOver(Look.WARNING, mark);
        int inset = (MARK - PICTURE) / 2;
        canvas.markOver(Look.icon("falls"), new Rect(mark.x() + inset, mark.y() + inset, PICTURE, PICTURE));
    }
}
