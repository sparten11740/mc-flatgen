package dev.flatgen.structures.client;

import dev.flatgen.client.gui.Icon;
import dev.flatgen.client.gui.TextButton;
import dev.flatgen.client.gui.Widget;
import dev.flatgen.client.gui.rules.FlowLayout;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.editor.client.panel.EditorLayout;
import dev.flatgen.editor.client.panel.EditorPanel;
import dev.flatgen.editor.client.panel.EditorRegion;
import dev.flatgen.mc.Structures;
import dev.flatgen.options.rules.OptionChoices;
import dev.flatgen.structures.rules.StructureChoice;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;

/** The editor's structures control: a button saying how the world chooses its structures, which opens their screen. */
public final class StructuresPanel implements EditorPanel {

    /** In Advanced, the structures' button on a box of the aside. */
    @Override
    public void layOut(EditorContext editor, EditorLayout layout) {
        if (!editor.advanced()) {
            return;
        }
        layout.box(inside -> content(editor, inside));
    }

    private List<Widget> content(EditorContext editor, FlowLayout flow) {
        return List.of(new TextButton(Icon.own("house"), () -> label(editor.shown().preset().structureSets()),
                flow.line(EditorRegion.ASIDE.rowHeight()), () -> open(editor), () -> true,
                () -> List.of(Component.translatable("flatgen.structures.open"))));
    }

    /** "Structures: the biome's", "Structures: none" or "Structures: 5 chosen". */
    private static Component label(Optional<List<String>> sets) {
        StructureChoice.Mode mode = StructureChoice.mode(sets);
        String key = switch (mode) {
            case BIOME -> "flatgen.structures.button.biome";
            case NONE -> "flatgen.structures.button.none";
            case CHOSEN -> "flatgen.structures.button.chosen";
        };
        return Component.translatable(key, sets.map(List::size).orElse(0));
    }

    private static void open(EditorContext editor) {
        editor.open(new StructuresScreen(editor, Structures.all(editor.registries()),
                OptionChoices.applied(OptionChoices.on(editor.shown().preset().options(), editor.worldOptions()))
                        .structureSets()));
    }
}
