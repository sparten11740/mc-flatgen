package dev.flatgen.editor.client.panel;

/** The sizes the editor's panels share, in GUI pixels, so their rows line up. */
public final class PanelSizes {
    /** The width of a panel's buttons with a word on them, such as Undo. */
    public static final int WIDE_BUTTON = 70;
    /** The height of a row of controls. */
    public static final int ROW_HEIGHT = 20;
    /** Space between controls and between rows. */
    public static final int GAP = 4;
    /** The width of the panel's wide controls, such as a picture switch: two buttons and the gap between them. */
    public static final int DOUBLE_WIDTH = 2 * WIDE_BUTTON + GAP;

    private PanelSizes() {
    }
}
