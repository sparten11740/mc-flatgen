package dev.flatgen.dimensions.client;

import dev.flatgen.client.gui.Icon;
import dev.flatgen.client.gui.QuestionScreen;
import dev.flatgen.client.gui.TextButton;
import dev.flatgen.client.gui.Widget;
import dev.flatgen.client.gui.rules.FlowLayout;
import dev.flatgen.dimensions.rules.NewStacks;
import dev.flatgen.dimensions.rules.SetStack;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.editor.client.panel.EditorPanel;
import dev.flatgen.editor.client.panel.PanelSizes;
import dev.flatgen.editor.rules.EditorState;
import dev.flatgen.preset.rules.Dimension;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;

/**
 * The Nether's or the End's own control (F22): for one the game makes, a button that makes it flat with a first stack;
 * for a flat one, a button that gives it back to the game, after asking.
 */
public final class DimensionPanel implements EditorPanel {
    /** The picture of making a dimension flat: bumpy ground, an arrow, flat stripes. */
    private static final Icon MAKE_FLAT = Icon.own("make_flat");
    /** The picture of giving a dimension back to the game: flat stripes, an arrow, bumpy ground. */
    private static final Icon GIVE_BACK = Icon.own("give_back");

    /** Shown while the Nether or the End is edited. */
    @Override
    public boolean shows(EditorState state) {
        return state.edited() != Dimension.OVERWORLD;
    }

    @Override
    public List<Widget> layOut(EditorContext editor, FlowLayout flow) {
        flow.newRow();
        Dimension dimension = editor.shown().edited();
        if (editor.shown().stack().isEmpty()) {
            return List.of(new TextButton(MAKE_FLAT, () -> makeFlatName(dimension),
                    flow.next(PanelSizes.DOUBLE_WIDTH), () -> makeFlat(editor, dimension), () -> true,
                    () -> List.of(makeFlatName(dimension), Component.translatable("flatgen.dimension.make_flat_tip"))));
        }
        return List.of(new TextButton(GIVE_BACK, () -> Component.translatable("flatgen.dimension.give_back"),
                flow.next(PanelSizes.DOUBLE_WIDTH), () -> askToGiveBack(editor, dimension), () -> true,
                () -> List.of(Component.translatable("flatgen.dimension.give_back_tip"))));
    }

    /** Gives the dimension its first stack. */
    private static void makeFlat(EditorContext editor, Dimension dimension) {
        editor.apply(new SetStack(Optional.of(NewStacks.of(dimension))));
    }

    /** Asks before the flat dimension's layers go; undo would bring them back too. */
    private static void askToGiveBack(EditorContext editor, Dimension dimension) {
        editor.open(new QuestionScreen(editor.screen(), GIVE_BACK, DimensionPictures.name(dimension),
                List.of(Component.translatable("flatgen.dimension.give_back_question", DimensionPictures.name(
                        dimension))), () -> editor.apply(new SetStack(Optional.empty()))));
    }

    /** "Make the Nether flat" or "Make the End flat". */
    private static Component makeFlatName(Dimension dimension) {
        return Component.translatable("flatgen.dimension.make_flat", DimensionPictures.name(dimension));
    }
}
