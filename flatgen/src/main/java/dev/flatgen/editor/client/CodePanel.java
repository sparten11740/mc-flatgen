package dev.flatgen.editor.client;

import dev.flatgen.client.gui.Icon;
import dev.flatgen.client.gui.PictureButton;
import dev.flatgen.client.gui.rules.FlowLayout;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.editor.client.panel.EditorLayout;
import dev.flatgen.editor.client.panel.EditorPanel;
import dev.flatgen.editor.client.panel.EditorRegion;
import dev.flatgen.layers.rules.LayerEdits;
import dev.flatgen.mc.Clipboard;
import dev.flatgen.preset.rules.Layer;
import dev.flatgen.preset.rules.VanillaCode;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;

/** Copying the game's superflat code for the layers, and pasting one, which says which part is wrong when it is. */
final class CodePanel implements EditorPanel {

    /** In Advanced, copying and pasting the code, a picture button each, in the bar above the layers. */
    @Override
    public void layOut(EditorContext editor, EditorLayout layout) {
        if (!editor.advanced()) {
            return;
        }
        FlowLayout bar = layout.flow(EditorRegion.LAYERS_BAR);
        int size = EditorRegion.LAYERS_BAR.rowHeight();
        layout.add(EditorRegion.LAYERS_BAR, List.of(
                new PictureButton(Icon.line("copy"), bar.next(size), () -> copy(editor),
                        () -> List.of(Component.translatable("flatgen.code.copy"),
                                Component.translatable("flatgen.code.copy_tip"))),
                new PictureButton(Icon.line("paste"), bar.next(size), () -> paste(editor),
                        () -> List.of(Component.translatable("flatgen.code.paste"),
                                Component.translatable("flatgen.code.paste_tip")))));
    }

    /** Puts the game's superflat code for the layers and biome on the clipboard. */
    private static void copy(EditorContext editor) {
        Clipboard.set(VanillaCode.write(editor.shown().preset()));
        editor.say(Component.translatable("flatgen.code.copied"));
    }

    /**
     * Reads a superflat code from the clipboard and puts its layers and biome in place of the preset's, as one step to
     * undo; a code with a wrong part changes nothing and says which part is wrong.
     */
    private static void paste(EditorContext editor) {
        String code = Clipboard.get();
        VanillaCode.Reading reading = VanillaCode.read(code);
        List<VanillaCode.Problem> problems = new ArrayList<>(reading.problems());
        if (problems.isEmpty()) {
            problems.addAll(CodeChecks.unknownIds(reading, editor.shown().preset(), editor.registries()));
        }
        int height = reading.layers().stream().mapToInt(Layer::height).sum();
        if (problems.isEmpty() && height > editor.world().height()) {
            problems.add(new VanillaCode.Problem(VanillaCode.Kind.TOO_TALL, 0, code.length(), code));
        }
        if (problems.isEmpty()) {
            editor.apply(new LayerEdits.Replace(reading.layers(), reading.biome()));
        }
        editor.say(problems.isEmpty() ? Component.translatable("flatgen.code.pasted")
                : CodeChecks.describe(problems.get(0), problems.size() - 1, editor.world().height()));
    }
}
