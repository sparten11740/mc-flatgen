package dev.flatgen.dimensions.client;

import dev.flatgen.client.gui.Icon;
import dev.flatgen.client.gui.PictureTab;
import dev.flatgen.client.gui.Widget;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.preset.rules.Dimension;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;

/**
 * The tabs above the editor's column (F22), one for each dimension, which choose the stack the column and the layer
 * controls edit. A dimension with a stack of its own wears the flat badge, three stripes, on its tab.
 */
public final class DimensionTabs {
    /** The tabs' height, in GUI pixels. */
    public static final int HEIGHT = PictureTab.HEIGHT;

    private DimensionTabs() {
    }

    /** The tabs, side by side across this area, as wide as it allows. */
    public static List<Widget> layOut(EditorContext editor, Rect area) {
        Dimension[] dimensions = Dimension.values();
        int width = area.width() / dimensions.length;
        List<Widget> tabs = new ArrayList<>();
        for (int index = 0; index < dimensions.length; index++) {
            Dimension dimension = dimensions[index];
            Icon plain = DimensionPictures.of(dimension);
            Icon flat = Icon.badged(plain, "badge_flat");
            Icon picture = (canvas, x, y) -> (hasOwnStack(editor, dimension) ? flat : plain).draw(canvas, x, y);
            tabs.add(new PictureTab(picture, new Rect(area.x() + index * width, area.y(), width, area.height()),
                    () -> editor.shown().edited() == dimension, () -> editor.choose(dimension),
                    () -> tooltip(editor, dimension)));
        }
        return tabs;
    }

    /** A tab's tooltip: the dimension's name, then whether it is flat or as the game makes it. */
    private static List<Component> tooltip(EditorContext editor, Dimension dimension) {
        return List.of(DimensionPictures.name(dimension), Component.translatable(hasOwnStack(editor, dimension)
                ? "flatgen.dimension.flat" : "flatgen.dimension.game_made"));
    }

    /** Whether the Nether or End has a stack of its own; the overworld always has, and wears no badge for it. */
    private static boolean hasOwnStack(EditorContext editor, Dimension dimension) {
        return dimension != Dimension.OVERWORLD && editor.shown().preset().stack(dimension).isPresent();
    }
}
