package dev.flatgen.client.gui;

/**
 * The kinds of text Flatgen's screens draw, each in the Inter font Flatgen ships at a size of its own. Each is defined
 * once for every count of window pixels a GUI pixel can be up to {@link #SHARPEST}, as {@code
 * flatgen:<name>_<pixels>}, its glyphs drawn at that many pixels so text stays sharp at every {@link
 * dev.flatgen.client.gui.rules.LayoutScale}; {@code tools/modern_look.py} writes the definitions.
 */
public enum TextStyle {
    /** Most text: labels, names, values. */
    BODY("body", 9),
    /** Text that stands out within a line: a chosen tab, a card's name. */
    STRONG("strong", 9),
    /** A screen's or a section's heading. */
    TITLE("title", 13);

    /** The most window pixels per GUI pixel the fonts are defined for; beyond, the sharpest is drawn larger. */
    public static final int SHARPEST = 6;

    private final String name;
    private final int lineHeight;

    TextStyle(String name, int lineHeight) {
        this.name = name;
        this.lineHeight = lineHeight;
    }

    /** The font that draws this text at this many window pixels per GUI pixel. */
    public String font(int pixelsPerUnit) {
        return "flatgen:" + name + "_" + Math.min(Math.max(pixelsPerUnit, 1), SHARPEST);
    }

    /** How tall a line of it is, in GUI pixels. */
    public int lineHeight() {
        return lineHeight;
    }
}
