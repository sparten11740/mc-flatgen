package dev.flatgen.client.gui.rules;

import java.util.ArrayList;
import java.util.List;

/**
 * The frame of a Flatgen screen, as the design canvas draws it: a header with the title across the top, a footer with
 * the screen's actions at its right across the bottom, a line between each and the body, and the body between them.
 *
 * @param width  the screen's width, in GUI pixels
 * @param height the screen's height, in GUI pixels
 */
public record PageFrame(int width, int height) {
    /** The header's and the footer's heights, in GUI pixels. */
    public static final int HEADER = 32;
    public static final int FOOTER = 40;
    /** Space at the screen's sides, and between the body and the lines above and below it, in GUI pixels. */
    public static final int MARGIN = 13;
    public static final int GAP = 11;
    /** The height of the footer's buttons, and the space between them, in GUI pixels. */
    public static final int ACTION_HEIGHT = 19;
    private static final int ACTION_GAP = 4;

    /** Where the body is, between the header and the footer, inside the margins. */
    public Rect body() {
        return new Rect(MARGIN, HEADER + GAP, width - 2 * MARGIN, height - HEADER - FOOTER - 2 * GAP);
    }

    /** The footer's row of buttons across the screen inside the margins. */
    public Rect footerRow() {
        return new Rect(MARGIN, height - FOOTER + (FOOTER - ACTION_HEIGHT) / 2, width - 2 * MARGIN, ACTION_HEIGHT);
    }

    /** The places of the footer's actions this wide, left to right, the last at the footer's right edge. */
    public List<Rect> actions(int... widths) {
        Rect row = footerRow();
        int x = row.right();
        for (int actionWidth : widths) {
            x -= actionWidth + ACTION_GAP;
        }
        List<Rect> places = new ArrayList<>();
        for (int actionWidth : widths) {
            x += ACTION_GAP;
            places.add(new Rect(x, row.y(), actionWidth, ACTION_HEIGHT));
            x += actionWidth;
        }
        return places;
    }
}
