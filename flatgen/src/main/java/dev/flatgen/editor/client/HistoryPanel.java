package dev.flatgen.editor.client;

import dev.flatgen.client.gui.TextButton;
import dev.flatgen.client.gui.Widget;
import dev.flatgen.client.gui.rules.FlowLayout;
import dev.flatgen.client.gui.rules.Modifiers;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.editor.client.panel.EditorPanel;
import dev.flatgen.editor.client.panel.PanelSizes;
import java.util.List;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/** Undo and redo, as buttons and as Control+Z, Control+Y and Control+Shift+Z. */
final class HistoryPanel implements EditorPanel {
    @Override
    public List<Widget> layOut(EditorContext editor, FlowLayout flow) {
        flow.newRow();
        return List.of(
                new TextButton(Component.translatable("flatgen.editor.undo"), flow.next(PanelSizes.WIDE_BUTTON),
                        editor::undo, editor::canUndo, List::of),
                new TextButton(Component.translatable("flatgen.editor.redo"), flow.next(PanelSizes.WIDE_BUTTON),
                        editor::redo, editor::canRedo, List::of));
    }

    @Override
    public boolean shortcut(EditorContext editor, int keyCode, Modifiers modifiers) {
        boolean redo = keyCode == GLFW.GLFW_KEY_Y || (keyCode == GLFW.GLFW_KEY_Z && modifiers.shift());
        if (!modifiers.control() || (keyCode != GLFW.GLFW_KEY_Z && keyCode != GLFW.GLFW_KEY_Y)) {
            return false;
        }
        if (redo) {
            editor.redo();
        } else {
            editor.undo();
        }
        return true;
    }
}
