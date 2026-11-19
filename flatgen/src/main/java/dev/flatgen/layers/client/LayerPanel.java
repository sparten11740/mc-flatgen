package dev.flatgen.layers.client;

import dev.flatgen.client.gui.Icon;
import dev.flatgen.client.gui.Label;
import dev.flatgen.client.gui.Look;
import dev.flatgen.client.gui.NumberField;
import dev.flatgen.client.gui.ScrollGroup;
import dev.flatgen.client.gui.Widget;
import dev.flatgen.client.gui.rules.FlowLayout;
import dev.flatgen.client.gui.rules.Modifiers;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.editor.client.panel.EditorLayout;
import dev.flatgen.editor.client.panel.EditorPanel;
import dev.flatgen.editor.client.panel.EditorRegion;
import dev.flatgen.editor.rules.EditorState;
import dev.flatgen.layers.rules.LayerEdits;
import java.util.List;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/**
 * The editor's layers: their cards ({@link LayerCards}) in the middle, adding a layer on top in the bar above them and,
 * in Advanced, the ground's Y to type; and their keys (arrows, Alt with arrows, Delete, Insert, Control+D).
 */
public final class LayerPanel implements EditorPanel {
    /** How wide the field to type the ground's Y is, in GUI pixels. */
    private static final int FIELD = 40;

    /** Shown for every dimension the preset gives a stack of its own (F22). */
    @Override
    public boolean shows(EditorState state) {
        return state.stack().isPresent();
    }

    /**
     * The cards in the layers' place, scrolled to show the chosen one, as they are laid out again whenever a layer is
     * added or removed; the bar's controls above them.
     */
    @Override
    public void layOut(EditorContext editor, EditorLayout layout) {
        if (editor.advanced()) {
            layout.add(EditorRegion.LAYERS_BAR, groundRow(editor, layout.flow(EditorRegion.LAYERS_BAR)));
        }
        layout.button(EditorRegion.LAYERS_BAR, Icon.line("plus"), Component.translatable("flatgen.editor.add_on_top"),
                () -> addOnTop(editor), () -> LayerText.roomLeft(editor.shown(), editor.world()) > 0,
                () -> LayerText.whyFull(editor.shown(), editor.world())).kind(Look.ButtonKind.ACCENT);
        Rect area = layout.area(EditorRegion.LAYERS);
        ScrollGroup cards = new ScrollGroup(area, top -> LayerCards.layOut(editor, area, top));
        LayerCards.chosen(editor, area).ifPresent(cards::reveal);
        layout.add(EditorRegion.LAYERS, cards);
    }

    @Override
    public boolean shortcut(EditorContext editor, int keyCode, Modifiers modifiers) {
        int selected = editor.shown().selected();
        Runnable action = switch (keyCode) {
            case GLFW.GLFW_KEY_UP -> modifiers.alt() ? () -> editor.apply(new LayerEdits.Move(selected + 1))
                    : () -> editor.select(selected + 1);
            case GLFW.GLFW_KEY_DOWN -> modifiers.alt() ? () -> editor.apply(new LayerEdits.Move(selected - 1))
                    : () -> editor.select(Math.max(0, selected - 1));
            case GLFW.GLFW_KEY_DELETE, GLFW.GLFW_KEY_BACKSPACE -> () -> editor.apply(new LayerEdits.Remove());
            case GLFW.GLFW_KEY_INSERT -> () -> editor.apply(new LayerEdits.Add(true));
            case GLFW.GLFW_KEY_D -> modifiers.control() ? () -> editor.apply(new LayerEdits.Duplicate()) : null;
            default -> null;
        };
        if (action == null) {
            return false;
        }
        action.run();
        return true;
    }

    /** The ground's Y to type, with its label: the top layer's depth changes so the ground is there. */
    private static List<Widget> groundRow(EditorContext editor, FlowLayout bar) {
        Component label = Component.translatable("flatgen.editor.ground_field");
        Rect place = bar.next(Label.width(editor.textWidth(label)));
        return List.of(new Label(Icon.item("minecraft:ladder"), label, place),
                new NumberField("surface", label, bar.next(FIELD),
                        () -> editor.world().floor() + editor.shown().height(),
                        y -> editor.apply(new LayerEdits.SurfaceAt(y))));
    }

    /** Adds a layer above the top one, as one step to undo. */
    private static void addOnTop(EditorContext editor) {
        editor.select(editor.shown().layers().size() - 1);
        editor.apply(new LayerEdits.Add(true));
    }
}
