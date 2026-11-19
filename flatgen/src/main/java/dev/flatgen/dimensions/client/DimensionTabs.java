package dev.flatgen.dimensions.client;

import dev.flatgen.client.gui.Icon;
import dev.flatgen.client.gui.SegmentedChoice;
import dev.flatgen.client.gui.TextStyle;
import dev.flatgen.client.gui.Widget;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.preset.rules.Dimension;
import java.util.Arrays;
import java.util.List;
import net.minecraft.network.chat.Component;

/**
 * The dimensions as a segmented choice in the editor's header (F22), each its picture and its name, which choose the
 * stack the editor edits. A dimension with a stack of its own wears the flat badge, three stripes, on its picture.
 */
public final class DimensionTabs {
    /** The tabs' height with their track, in GUI pixels. */
    public static final int HEIGHT = SegmentedChoice.HEIGHT;

    private DimensionTabs() {
    }

    /** How wide the tabs are with their track, in GUI pixels, each wide enough for its name in bold, as when chosen. */
    public static int width(EditorContext editor) {
        return SegmentedChoice.width(options(editor));
    }

    /** The track and the tabs on it, side by side from the left of this area. */
    public static List<Widget> layOut(EditorContext editor, Rect area) {
        return SegmentedChoice.layOut(options(editor), area);
    }

    /** A segment for each dimension. */
    private static List<SegmentedChoice.Option> options(EditorContext editor) {
        return Arrays.stream(Dimension.values()).map(dimension -> option(editor, dimension)).toList();
    }

    private static SegmentedChoice.Option option(EditorContext editor, Dimension dimension) {
        Icon plain = DimensionPictures.of(dimension);
        Icon flat = Icon.badged(plain, "badge_flat");
        Component name = DimensionPictures.name(dimension);
        return new SegmentedChoice.Option((canvas, x, y) -> (hasOwnStack(editor, dimension) ? flat : plain)
                .draw(canvas, x, y), () -> name, editor.textWidth(name, TextStyle.STRONG),
                () -> editor.shown().edited() == dimension, () -> editor.choose(dimension),
                () -> tooltip(editor, dimension));
    }

    /** A tab's tooltip: the dimension's name, then whether it is flat, made by a data pack or as the game makes it. */
    private static List<Component> tooltip(EditorContext editor, Dimension dimension) {
        return List.of(DimensionPictures.name(dimension), Component.translatable(stateKey(editor, dimension)));
    }

    /** The words for a dimension's state on its tab, as a translation key. */
    private static String stateKey(EditorContext editor, Dimension dimension) {
        if (dimension != Dimension.OVERWORLD && editor.packDecides(dimension)) {
            return "flatgen.dimension.pack_decides";
        }
        return hasOwnStack(editor, dimension) ? "flatgen.dimension.flat" : "flatgen.dimension.game_made";
    }

    /** Whether the Nether or End has a stack of its own; the overworld always has, and wears no badge for it. */
    private static boolean hasOwnStack(EditorContext editor, Dimension dimension) {
        return dimension != Dimension.OVERWORLD && editor.shown().preset().stack(dimension).isPresent();
    }
}
