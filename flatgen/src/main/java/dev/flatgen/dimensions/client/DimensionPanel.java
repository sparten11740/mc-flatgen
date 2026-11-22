package dev.flatgen.dimensions.client;

import dev.flatgen.client.gui.Caption;
import dev.flatgen.client.gui.Chip;
import dev.flatgen.client.gui.Icon;
import dev.flatgen.client.gui.Label;
import dev.flatgen.client.gui.Look;
import dev.flatgen.client.gui.QuestionScreen;
import dev.flatgen.client.gui.Surface;
import dev.flatgen.client.gui.TextButton;
import dev.flatgen.client.gui.TextStyle;
import dev.flatgen.client.gui.Widget;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.dimensions.rules.NewStacks;
import dev.flatgen.dimensions.rules.SetStack;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.editor.client.panel.EditorLayout;
import dev.flatgen.editor.client.panel.EditorPanel;
import dev.flatgen.editor.client.panel.EditorRegion;
import dev.flatgen.preset.rules.Dimension;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;

/**
 * The Nether's or the End's own controls (F22): for one the game makes, the dimension as the game makes it and a button
 * that makes it flat with a first stack; for a flat one, a button that gives it back to the game, after asking. When a
 * data pack makes the dimension, it says so in place of making it flat, as the pack's dimension would win.
 */
public final class DimensionPanel implements EditorPanel {
    /** The picture of making a dimension flat: bumpy ground, an arrow, flat stripes. */
    private static final Icon MAKE_FLAT = Icon.own("make_flat");
    /** The picture of giving a dimension back to the game: flat stripes, an arrow, bumpy ground. */
    private static final Icon GIVE_BACK = Icon.own("give_back");
    /** The picture of a data pack, as the library's Datapack button has it. */
    /** Space between the parts of the empty state, in GUI pixels. */
    private static final int SPACING = 6;
    /** How tall the button that makes a dimension flat is, in GUI pixels. */
    private static final int MAKE_FLAT_HEIGHT = 21;
    /** How tall the empty state is, in GUI pixels: picture, heading, line, button and the space between. */
    private static final int EMPTY_STATE_HEIGHT = Icon.SIZE + 4 * SPACING + TextStyle.TITLE.lineHeight()
            + TextStyle.BODY.lineHeight() + MAKE_FLAT_HEIGHT;

    /** Shown while the Nether or the End is edited. */
    @Override
    public boolean shows(EditorContext editor) {
        return editor.shown().edited() != Dimension.OVERWORLD;
    }

    /**
     * For a dimension the game makes, the dimension as the game makes it in the world's place, and in the layers'
     * place what that means and the button that makes it flat; for one a data pack makes, that the pack makes it; for
     * a flat one, giving it back on a box of the aside.
     */
    @Override
    public void layOut(EditorContext editor, EditorLayout layout) {
        Dimension dimension = editor.shown().edited();
        if (editor.shown().stack().isPresent()) {
            layout.buttonBox(GIVE_BACK, Component.translatable("flatgen.dimension.give_back"),
                    () -> askToGiveBack(editor, dimension),
                    () -> List.of(Component.translatable("flatgen.dimension.give_back_tip")));
            return;
        }
        Rect area = layout.area(EditorRegion.LAYERS);
        if (editor.packDecides(dimension)) {
            layout.add(EditorRegion.LAYERS, packState(area));
            return;
        }
        Rect world = layout.area(EditorRegion.WORLD);
        layout.add(EditorRegion.WORLD, List.of(new GameMadePicture(dimension, world), Chip.inCorner(
                DimensionPictures.of(dimension), () -> Component.translatable("flatgen.dimension.game_made"), world)));
        layout.add(EditorRegion.LAYERS, emptyState(editor, dimension, area));
    }

    /** In the middle of the layers' place: the dimension's picture, that the game makes it, and making it flat. */
    private static List<Widget> emptyState(EditorContext editor, Dimension dimension, Rect area) {
        int top = area.y() + (area.height() - EMPTY_STATE_HEIGHT) / 2;
        Component name = DimensionPictures.name(dimension);
        Rect line = new Rect(area.x(), top + Icon.SIZE + 2 * SPACING + TextStyle.TITLE.lineHeight(), area.width(),
                TextStyle.BODY.lineHeight());
        int buttonWidth = TextButton.width(editor.textWidth(makeFlatName(dimension)));
        Rect button = new Rect(area.x() + (area.width() - buttonWidth) / 2, line.bottom() + 2 * SPACING, buttonWidth,
                MAKE_FLAT_HEIGHT);
        return List.of(new Surface(Look.PANEL, area), picture(DimensionPictures.of(dimension), area, top),
                new Caption(() -> Component.translatable("flatgen.dimension.game_made_title", name), new Rect(area.x(),
                        top + Icon.SIZE + SPACING, area.width(), TextStyle.TITLE.lineHeight()), TextStyle.TITLE,
                        Look.TEXT, true),
                new Caption(() -> Component.translatable("flatgen.dimension.make_flat_tip"), line, TextStyle.BODY,
                        Look.MUTED, true),
                new TextButton(MAKE_FLAT, () -> makeFlatName(dimension), button,
                        () -> makeFlat(editor, dimension), () -> true, () -> List.of(makeFlatName(dimension)))
                        .kind(Look.ButtonKind.VIVID));
    }

    /** In the middle of the layers' place: a data pack's picture, and that the pack makes this dimension. */
    private static List<Widget> packState(Rect area) {
        int top = area.y() + (area.height() - Icon.SIZE - SPACING - TextStyle.TITLE.lineHeight()) / 2;
        return List.of(new Surface(Look.PANEL, area), picture(Icon.DATAPACK, area, top),
                new Caption(() -> Component.translatable("flatgen.dimension.pack_decides"), new Rect(area.x(),
                        top + Icon.SIZE + SPACING, area.width(), TextStyle.TITLE.lineHeight()), TextStyle.TITLE,
                        Look.TEXT, true));
    }

    /** A picture in the middle of an area's width, its top here. */
    private static Widget picture(Icon icon, Rect area, int top) {
        return new Label(icon, Component.empty(), new Rect(area.x() + (area.width() - Icon.SIZE) / 2, top, Icon.SIZE,
                Icon.SIZE));
    }

    /** Gives the dimension its first stack. */
    private static void makeFlat(EditorContext editor, Dimension dimension) {
        editor.apply(new SetStack(Optional.of(NewStacks.of(dimension))));
    }

    /** Asks before the flat dimension's layers go; undo would bring them back too. */
    private static void askToGiveBack(EditorContext editor, Dimension dimension) {
        Component name = DimensionPictures.name(dimension);
        editor.open(new QuestionScreen(editor.screen(), GIVE_BACK, name,
                List.of(Component.translatable("flatgen.dimension.give_back_question", name)),
                () -> editor.apply(new SetStack(Optional.empty()))));
    }

    /** "Make the Nether flat" or "Make the End flat". */
    private static Component makeFlatName(Dimension dimension) {
        return Component.translatable("flatgen.dimension.make_flat", DimensionPictures.name(dimension));
    }
}
