package dev.flatgen.client.gui;

/**
 * The kinds of text Flatgen's screens draw, each in the Inter font Flatgen ships at a size of its own. Each is defined
 * once for every count of window pixels a GUI pixel can be up to {@link #SHARPEST}, as {@code
 * flatgen:<name>_<pixels>}, its glyphs drawn at that many pixels so text stays sharp at every {@link
 * dev.flatgen.client.gui.rules.LayoutScale}; {@code tools/modern_look.py} writes the definitions.
 */
public enum TextStyle {
    /** Most text: labels, names, values. */
    BODY("body", 9, 1),
    /** Text that stands out within a line: a chosen tab, a card's name. */
    STRONG("strong", 9, 1),
    /** A screen's or a section's heading. */
    TITLE("title", 13, 3);

    /** The most window pixels per GUI pixel the fonts are defined for; beyond, the sharpest is drawn larger. */
    public static final int SHARPEST = 6;

    private final String name;
    private final int lineHeight;
    /** How far below a line's centred top the text is drawn, in GUI pixels, so its letters sit in the middle. */
    private final int drop;

    TextStyle(String name, int lineHeight, int drop) {
        this.name = name;
        this.lineHeight = lineHeight;
        this.drop = drop;
    }

    /** The font that draws this text at this many window pixels per GUI pixel. */
    public String font(int pixelsPerUnit) {
        return "flatgen:" + name + "_" + Math.min(Math.max(pixelsPerUnit, 1), SHARPEST);
    }

    /** Where to draw a line of it so its letters sit in the middle of a row this tall from this top. */
    public int top(int rowTop, int rowHeight) {
        return rowTop + (rowHeight - lineHeight) / 2 + drop;
    }

    /**
     * Where to draw a picture beside a line of it in a row this tall from this top, so that the picture's middle is
     * level with the middle of the letters, half a line below the line's top: each centred on its own, the two round
     * apart on a row of odd height. A picture without words sits there too, so a row of buttons lines up.
     */
    public int pictureTop(int rowTop, int rowHeight) {
        return top(rowTop, rowHeight) + lineHeight / 2 - Icon.SIZE / 2;
    }

    /** How tall a line of it is, in GUI pixels. */
    public int lineHeight() {
        return lineHeight;
    }
}
