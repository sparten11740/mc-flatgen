package dev.flatgen.client.gui.rules;

/**
 * How big Flatgen's screens draw, apart from the game's GUI scale: a Flatgen screen counts in GUI pixels of its own, a
 * whole number of window pixels each, as many as leave it at least {@link #MIN_WIDTH} by {@link #MIN_HEIGHT}. Every
 * window then gets a roomy layout, drawn as the design canvas has it at a third of 1920 by 1080, at a size that stays
 * readable where the game's own scale left most players a cramped one (docs/design/simple-editor.md); whole pixels keep
 * lines, rounded corners and text sharp. The game still places the mouse, tooltips and scissors in its own GUI pixels,
 * which this converts to.
 *
 * @param pixelsPerUnit how many window pixels a Flatgen GUI pixel is wide and tall
 * @param width         how wide the screen is, in Flatgen GUI pixels
 * @param height        how tall the screen is, in Flatgen GUI pixels
 * @param gameUnit      how many of the game's GUI pixels a Flatgen GUI pixel is
 */
public record LayoutScale(int pixelsPerUnit, int width, int height, float gameUnit) {
    /** The narrowest a Flatgen screen gets, in its GUI pixels, unless the window is narrower still. */
    public static final int MIN_WIDTH = 640;
    /** The shortest a Flatgen screen gets, in its GUI pixels, unless the window is shorter still. */
    public static final int MIN_HEIGHT = 360;

    /** The scale for a window this many pixels wide and tall, where the game's GUI pixel is this many of them. */
    public static LayoutScale of(int windowWidth, int windowHeight, double gameScale) {
        int perUnit = Math.max(1, Math.min(windowWidth / MIN_WIDTH, windowHeight / MIN_HEIGHT));
        return new LayoutScale(perUnit, windowWidth / perUnit, windowHeight / perUnit, (float) (perUnit / gameScale));
    }

    /** Where a coordinate of the screen is in the game's GUI pixels. */
    public int toGame(int coordinate) {
        return Math.round(coordinate * gameUnit);
    }

    /** Where an area of the screen is in the game's GUI pixels. */
    public Rect toGame(Rect area) {
        return new Rect(toGame(area.x()), toGame(area.y()), toGame(area.width()), toGame(area.height()));
    }

    /** Where a coordinate the game gives in its GUI pixels, such as the mouse's, is on the screen. */
    public double fromGame(double coordinate) {
        return coordinate / gameUnit;
    }
}
