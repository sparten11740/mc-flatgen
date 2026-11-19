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
    /** Where the last thing placed ends, its bottom edge; the area's top before anything is placed. */
    private int placedBottom;
    /** Whether the current row has a widget yet. */
    private boolean rowStarted;

    /** Rows of widgets this tall, this far apart, from the area's top left corner down within its width. */
    public FlowLayout(Rect area, int rowHeight, int gap) {
        this.area = area;
        this.rowHeight = rowHeight;
        this.gap = gap;
        this.x = area.x();
        this.y = area.y();
        this.placedBottom = area.y();
    }

    /** The place of the next widget, this wide, on this row or, when it does not fit, the next. */
    public Rect next(int width) {
        if (rowStarted && x + width > area.right()) {
            newRow();
        }
        Rect place = new Rect(x, y, width, rowHeight);
        x += width + gap;
        rowStarted = true;
        placedBottom = place.bottom();
        return place;
    }

    /** A row of its own across the area's width, this tall, such as a line of text. */
    public Rect line(int height) {
        newRow();
        Rect place = new Rect(area.x(), y, area.width(), height);
        y += height + gap;
        placedBottom = place.bottom();
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

    /**
     * A flow inside a box across this flow's width from the next row, placing within it this far in from its edges;
     * {@link #endBox} closes the box once the inner flow has placed everything.
     */
    public FlowLayout boxed(int padding) {
        newRow();
        return new FlowLayout(new Rect(area.x() + padding, y + padding, area.width() - 2 * padding, 0), rowHeight,
                gap);
    }

    /** The rest of the current row, from where the next widget would go to the area's right edge. */
    public Rect rest() {
        return new Rect(x, y, area.right() - x, rowHeight);
    }

    /**
     * Closes a box opened with {@link #boxed}: where it is, as far below what the inner flow placed as the padding,
     * after which this flow carries on below it.
     */
    public Rect endBox(FlowLayout inner, int padding) {
        Rect box = new Rect(area.x(), y, area.width(), inner.placedBottom + padding - y);
        y = box.bottom() + gap;
        x = area.x();
        rowStarted = false;
        return box;
    }

    /** Where what was placed ends, just below its last row. */
    public int bottom() {
        return rowStarted ? y + rowHeight : y;
    }
}
