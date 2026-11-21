package dev.flatgen.client.gui;

/**
 * Flatgen's look (F29): the colours screens draw text and fills in and the sprites they draw, by name, so widgets
 * never spell either out. Dark surfaces with rounded corners, light text, blue for what is chosen and green for going
 * ahead, as drawn on the design canvas; {@code tools/modern_look.py} draws the sprites in the same colours. ARGB.
 */
public final class Look {
    /** Behind everything on a Flatgen screen. */
    public static final int BACKGROUND = 0xFF111214;
    /** Over the screen a picker or a question stands over, which shows dimmed through it. */
    public static final int SCRIM = 0xB8080909;
    /** A line between parts of a screen, such as under its header. */
    public static final int DIVIDER = 0xFF23262B;
    /** Most text. */
    public static final int TEXT = 0xFFE8EAED;
    /** Text that says less: a caption, a hint, a unit. */
    public static final int MUTED = 0xFF9AA0A6;
    /** Text on something that cannot be used now. */
    public static final int DISABLED = 0xFF5F656D;
    /** What is chosen, and a link. */
    public static final int ACCENT = 0xFF5AA9FF;
    /** Text on the green button that goes ahead, on the blue one that adds, and on the purple one that stands out. */
    public static final int ON_PRIMARY = 0xFFFFFFFF;
    public static final int ON_ACCENT = 0xFFD6E7FF;
    public static final int ON_VIVID = 0xFFFFFFFF;
    /** A button's edge, and the line parting the parts of a box several buttons share. */
    public static final int EDGE = 0xFF33373E;
    /** The dashed edge of what is not there yet, such as the row a new layer would take. */
    public static final int GHOST_EDGE = 0xFF4A5059;

    /** A raised surface holding a group of controls. */
    public static final String PANEL = sprite("panel");
    /** A surface over the rest of the screen, such as a picker. */
    public static final String SHEET = sprite("sheet");
    /** A card that is chosen, and the ring around a picture button that is. */
    public static final String CHOSEN = sprite("card_chosen");
    public static final String CHOSEN_RING = sprite("ring_chosen");
    /** The faint face of what is not there yet, and its face while the mouse is over it or it has the focus. */
    public static final String GHOST = sprite("ghost");
    public static final String GHOST_LIT = sprite("ghost_lit");
    /** A block not there yet, chequered, as large as a block's picture. */
    public static final String PLACEHOLDER_BLOCK = sprite("placeholder_block");
    /** A plain button's face, and its face while the mouse is over it. */
    public static final String BUTTON = sprite("button");
    public static final String BUTTON_LIT = sprite("button_lit");
    /** A field square and flat between a stepper's buttons, the box they share keeping its edge along it. */
    public static final String FIELD_BAND = sprite("field_band");
    /** A text field's box, and the box of the one that has the keyboard. */
    public static final String FIELD = sprite("field");
    public static final String FIELD_LIT = sprite("field_lit");
    /** The pill behind a count. */
    public static final String COUNT = sprite("count");
    /** A dark pill behind a fact over a picture. */
    public static final String CHIP = sprite("chip");
    /** A yellow pill behind a warning's picture, such as a block that falls. */
    public static final String WARNING = sprite("warning");
    /** Behind a tooltip's words. */
    public static final String TOOLTIP = sprite("tooltip");
    /** The ring around what has the keyboard's focus. */
    public static final String FOCUS = sprite("focus");
    /** The track of segmented choices, such as the dimensions. */
    public static final String SEGMENTS = sprite("segments");
    /** The chosen segment on that track. */
    public static final String SEGMENT_ON = sprite("segment_on");
    /** A switch's track while on and while off, and its knob. */
    public static final String SWITCH_ON = sprite("switch_on");
    public static final String SWITCH_OFF = sprite("switch_off");
    public static final String KNOB = sprite("knob");

    private Look() {
    }

    /** One of the look's icons by its name, a line drawing to draw at any size. */
    public static String icon(String name) {
        return sprite("icon_" + name);
    }

    /** A button's face in one of the look's kinds, as drawn while hovered, normally, or while it cannot be used. */
    public static String button(ButtonKind kind, boolean hovered, boolean usable) {
        return !usable ? sprite("button_off") : sprite(kind.sprite + (hovered ? "_lit" : ""));
    }

    private static String sprite(String name) {
        return "flatgen:modern/" + name;
    }

    /** What a button does, which its colour says. */
    public enum ButtonKind {
        /** Most buttons. */
        PLAIN("button", TEXT),
        /** The one that goes ahead, such as Done. */
        PRIMARY("button_primary", ON_PRIMARY),
        /** One that adds, such as a new layer. */
        ACCENT("button_accent", ON_ACCENT),
        /** The one thing to do on an otherwise empty screen, such as making a dimension flat. */
        VIVID("button_vivid", ON_VIVID),
        /** One that does what cannot be undone, such as deleting, after asking. */
        DANGER("button_danger", ON_VIVID);

        private final String sprite;
        private final int label;

        ButtonKind(String sprite, int label) {
            this.sprite = sprite;
            this.label = label;
        }

        /** The colour of the label on it. */
        public int label() {
            return label;
        }
    }
}
