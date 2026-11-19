package dev.flatgen.client.gui;

import dev.flatgen.client.gui.rules.Rect;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;

/**
 * One choice of a few, as segments side by side on a track, each a picture and a name, the chosen one raised
 * ({@link PictureTab}): the editor's dimensions, the structures' modes.
 */
public final class SegmentedChoice {
    /** Space around the segments inside their track, in GUI pixels. */
    private static final int TRACK_PADDING = 2;
    /** The track's height, in GUI pixels. */
    public static final int HEIGHT = PictureTab.HEIGHT + 2 * TRACK_PADDING;

    private SegmentedChoice() {
    }

    /**
     * One segment.
     *
     * @param icon      its picture
     * @param name      its name, which may change
     * @param nameWidth how wide its name is drawn in bold, as when chosen, in GUI pixels
     * @param chosen    whether it is the chosen one now
     * @param choose    what choosing it does
     * @param tooltip   what its tooltip says
     */
    public record Option(Icon icon, Supplier<Component> name, int nameWidth, BooleanSupplier chosen, Runnable choose,
            Supplier<List<Component>> tooltip) {
    }

    /** How wide the track is for these segments, in GUI pixels. */
    public static int width(List<Option> options) {
        return 2 * TRACK_PADDING + options.stream().mapToInt(option -> PictureTab.width(option.nameWidth())).sum();
    }

    /** The track and its segments, side by side from the left of this area. */
    public static List<Widget> layOut(List<Option> options, Rect area) {
        List<Widget> widgets = new ArrayList<>(List.of(new Surface(Look.SEGMENTS, area)));
        int x = area.x() + TRACK_PADDING;
        for (Option option : options) {
            int width = PictureTab.width(option.nameWidth());
            widgets.add(new PictureTab(option.icon(), option.name(), new Rect(x, area.y() + TRACK_PADDING, width,
                    PictureTab.HEIGHT), option.chosen(), option.choose(), option.tooltip()));
            x += width;
        }
        return widgets;
    }
}
