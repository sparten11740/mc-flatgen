package dev.flatgen.editor.client;

import dev.flatgen.client.gui.Icon;
import dev.flatgen.client.gui.TextButton;
import dev.flatgen.client.gui.rules.FlowLayout;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.editor.client.panel.EditorLayout;
import dev.flatgen.editor.client.panel.EditorPanel;
import dev.flatgen.editor.client.panel.EditorRegion;
import dev.flatgen.layers.client.ColumnPicture;
import dev.flatgen.layers.rules.LayerEdits;
import dev.flatgen.mc.Biomes;
import dev.flatgen.mc.Clipboard;
import dev.flatgen.preset.rules.Layer;
import dev.flatgen.preset.rules.VanillaCode;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;

/** Copying the game's superflat code for the layers, and pasting one, which says which part is wrong when it is. */
final class CodePanel implements EditorPanel {

    /** How many of the stack's top layers the code buttons' stack shows, a band each, and its inset in its icon. */
    private static final int PICTURED_LAYERS = 3;
    private static final int STACK_INSET = 4;

    /**
     * In Advanced, copying and pasting the code in the bar above the layers, each saying so in words beside pictures
     * that run opposite ways: the stack to the clipboard, and the clipboard to the stack.
     */
    @Override
    public void layOut(EditorContext editor, EditorLayout layout) {
        if (!editor.advanced()) {
            return;
        }
        FlowLayout bar = layout.flow(EditorRegion.LAYERS_BAR);
        Icon stack = (canvas, x, y) -> ColumnPicture.draw(canvas, topLayers(editor),
                Biomes.waterColor(editor.shown().biome(), editor.registries()),
                new Rect(x + STACK_INSET, y + 1, Icon.SIZE - 2 * STACK_INSET, Icon.SIZE - 2));
        Icon clipboard = Icon.line("clipboard");
        Icon arrow = Icon.line("arrow_right");
        layout.add(EditorRegion.LAYERS_BAR, List.of(
                codeButton(editor, bar, Icon.inRow(List.of(stack, arrow, clipboard)), "flatgen.code.copy",
                        "flatgen.code.copy_tip", () -> copy(editor)),
                codeButton(editor, bar, Icon.inRow(List.of(clipboard, arrow, stack)), "flatgen.code.paste",
                        "flatgen.code.paste_tip", () -> paste(editor))));
    }

    /** A button of the bar with these pictures and words, its tip saying what it does. */
    private static TextButton codeButton(EditorContext editor, FlowLayout bar, Icon picture, String words, String tip,
            Runnable action) {
        Component label = Component.translatable(words);
        return new TextButton(picture, () -> label, bar.next(TextButton.width(picture, editor.textWidth(label))),
                action, () -> true, () -> List.of(Component.translatable(tip)));
    }

    /** The stack's top layers, a block each, as the code buttons picture it: the others would be too thin to see. */
    private static List<Layer> topLayers(EditorContext editor) {
        List<Layer> layers = editor.shown().layers();
        return layers.subList(Math.max(0, layers.size() - PICTURED_LAYERS), layers.size()).stream()
                .map(layer -> new Layer(layer.block(), 1)).toList();
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
