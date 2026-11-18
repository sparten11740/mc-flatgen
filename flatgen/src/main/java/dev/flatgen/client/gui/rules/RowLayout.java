package dev.flatgen.client.gui.rules;

/** Places widgets of one row left to right, each a gap after the one before, for laying out a screen. */
public final class RowLayout {
    private final int y;
    private final int height;
    private final int gap;
    private int x;

    /** A row whose first widget starts at this point, its widgets this tall, this far apart. */
    public RowLayout(int x, int y, int height, int gap) {
        this.x = x;
        this.y = y;
        this.height = height;
        this.gap = gap;
    }

    /** The place of the next widget, this wide. */
    public Rect next(int width) {
        Rect place = new Rect(x, y, width, height);
        x += width + gap;
        return place;
    }
}
