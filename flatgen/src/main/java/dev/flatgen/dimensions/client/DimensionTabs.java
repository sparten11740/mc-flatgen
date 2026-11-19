package dev.flatgen.dimensions.client;

import dev.flatgen.client.gui.Icon;
import dev.flatgen.client.gui.Look;
import dev.flatgen.client.gui.PictureTab;
import dev.flatgen.client.gui.Surface;
import dev.flatgen.client.gui.TextStyle;
import dev.flatgen.client.gui.Widget;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.preset.rules.Dimension;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;

/**
 * The dimensions as a segmented choice in the editor's header (F22), each its picture and its name, which choose the
 * stack the editor edits. A dimension with a stack of its own wears the flat badge, three stripes, on its picture.
 */
public final class DimensionTabs {
    /** Space around the tabs inside their track, in GUI pixels. */
    private static final int TRACK_PADDING = 2;
    /** The tabs' height with their track, in GUI pixels. */
    public static final int HEIGHT = PictureTab.HEIGHT + 2 * TRACK_PADDING;

    private DimensionTabs() {
    }

    /** How wide the tabs are with their track, in GUI pixels, each wide enough for its name in bold, as when chosen. */
    public static int width(EditorContext editor) {
        int width = 2 * TRACK_PADDING;
        for (Dimension dimension : Dimension.values()) {
            width += PictureTab.width(editor.textWidth(DimensionPictures.name(dimension), TextStyle.STRONG));
        }
        return width;
    }

    /** The track and the tabs on it, side by side from the left of this area. */
    public static List<Widget> layOut(EditorContext editor, Rect area) {
        List<Widget> tabs = new ArrayList<>(List.of(new Surface(Look.SEGMENTS, area)));
        int x = area.x() + TRACK_PADDING;
        for (Dimension dimension : Dimension.values()) {
            Icon plain = DimensionPictures.of(dimension);
            Icon flat = Icon.badged(plain, "badge_flat");
            Icon picture = (canvas, left, top) -> (hasOwnStack(editor, dimension) ? flat : plain).draw(canvas, left,
                    top);
            int tabWidth = PictureTab.width(editor.textWidth(DimensionPictures.name(dimension), TextStyle.STRONG));
            tabs.add(new PictureTab(picture, () -> DimensionPictures.name(dimension), new Rect(x,
                    area.y() + TRACK_PADDING, tabWidth, PictureTab.HEIGHT), () -> editor.shown().edited() == dimension,
                    () -> editor.choose(dimension), () -> tooltip(editor, dimension)));
            x += tabWidth;
        }
        return tabs;
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
