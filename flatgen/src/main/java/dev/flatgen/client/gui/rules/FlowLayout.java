package dev.flatgen.client.gui.rules;

/**
 * Places a screen's widgets left to right in rows within an area's width, starting a new row when the next widget does
 * not fit, so a panel still shows everything on a narrow screen; how far down it went is where it ends.
 */
public final class FlowLayout {
    private final Rect area;
    private final int rowHeight;
    private final int gap;
    private int x;
    private int y;
    /** Whether the current row has a widget yet. */
    private boolean rowStarted;

    /** Rows of widgets this tall, this far apart, from the area's top left corner down within its width. */
    public FlowLayout(Rect area, int rowHeight, int gap) {
        this.area = area;
        this.rowHeight = rowHeight;
        this.gap = gap;
        this.x = area.x();
        this.y = area.y();
    }

    /** The place of the next widget, this wide, on this row or, when it does not fit, the next. */
    public Rect next(int width) {
        if (rowStarted && x + width > area.right()) {
            newRow();
        }
        Rect place = new Rect(x, y, width, rowHeight);
        x += width + gap;
        rowStarted = true;
        return place;
    }

    /** A row of its own across the area's width, this tall, such as a line of text. */
    public Rect line(int height) {
        newRow();
        Rect place = new Rect(area.x(), y, area.width(), height);
        y += height + gap;
        return place;
    }

    /** Starts the next widget on a row of its own. */
    public void newRow() {
        if (rowStarted) {
            y += rowHeight + gap;
        }
        x = area.x();
        rowStarted = false;
    }

    /** Where what was placed ends, just below its last row. */
    public int bottom() {
        return rowStarted ? y + rowHeight : y;
    }
}
