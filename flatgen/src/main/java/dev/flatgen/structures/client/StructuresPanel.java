package dev.flatgen.structures.client;

import dev.flatgen.client.gui.TextButton;
import dev.flatgen.client.gui.Widget;
import dev.flatgen.client.gui.rules.FlowLayout;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.editor.client.panel.EditorPanel;
import dev.flatgen.editor.client.panel.PanelSizes;
import dev.flatgen.mc.Structures;
import dev.flatgen.options.InstalledOptions;
import dev.flatgen.options.rules.OptionChoices;
import dev.flatgen.structures.rules.StructureChoice;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.network.chat.Component;

/** The editor's structures control: a button saying how the world chooses its structures, which opens their screen. */
public final class StructuresPanel implements EditorPanel {
    /** The width of the button, in GUI pixels: two of the panel's wide buttons and the gap between them. */
    private static final int BUTTON = 2 * PanelSizes.WIDE_BUTTON + PanelSizes.GAP;

    @Override
    public List<Widget> layOut(EditorContext editor, FlowLayout flow) {
        return List.of(new TextButton(() -> label(editor.shown().preset().structureSets()), flow.next(BUTTON),
                () -> open(editor), () -> true,
                () -> List.of(Component.translatable("flatgen.structures.open"))));
    }

    /** "Structures: the biome's", "Structures: none" or "Structures: 5 chosen". */
    private static Component label(Optional<List<String>> sets) {
        StructureChoice.Mode mode = StructureChoice.mode(sets);
        return Component.translatable("flatgen.structures.button." + mode.name().toLowerCase(Locale.ROOT),
                sets.map(List::size).orElse(0));
    }

    private static void open(EditorContext editor) {
        editor.open(new StructuresScreen(editor, Structures.all(editor.registries()),
                OptionChoices.applied(OptionChoices.on(editor.shown().preset().options(), InstalledOptions.load()))
                        .structureSets()));
    }
}
