package dev.flatgen.client.gui.rules;

/**
 * A rectangle of the screen in GUI pixels, which widgets are placed in and draw within.
 *
 * @param x      its left edge
 * @param y      its top edge
 * @param width  how wide it is
 * @param height how tall it is
 */
public record Rect(int x, int y, int width, int height) {
    /** Its right edge, just outside it. */
    public int right() {
        return x + width;
    }

    /** Its bottom edge, just outside it. */
    public int bottom() {
        return y + height;
    }

    /** Whether a point of the screen is inside it. */
    public boolean contains(double pointX, double pointY) {
        return pointX >= x && pointX < right() && pointY >= y && pointY < bottom();
    }
}
