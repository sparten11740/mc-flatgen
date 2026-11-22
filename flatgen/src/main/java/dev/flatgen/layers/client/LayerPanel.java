package dev.flatgen.layers.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.flatgen.client.gui.Icon;
import dev.flatgen.client.gui.Label;
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
import dev.flatgen.layers.rules.LayerEdits;
import java.util.List;
import net.minecraft.network.chat.Component;

/**
 * The editor's layers: their cards ({@link LayerCards}) in the middle, with the row that adds a layer above them,
 * and in Advanced the ground's Y to type in the bar above them; and their keys (arrows, Alt with arrows, Delete,
 * Insert, which adds a layer above the chosen one, and Control+D).
 */
public final class LayerPanel implements EditorPanel {
    /** How wide the field to type the ground's Y is, in GUI pixels. */
    private static final int FIELD = 40;

    /** How far the cards are scrolled, kept as the editor lays them out again, as an edit adds or removes a layer. */
    private final ScrollGroup.Position cardsScroll = new ScrollGroup.Position();

    /** Shown for every dimension the preset gives a stack of its own (F22). */
    @Override
    public boolean shows(EditorContext editor) {
        return editor.shown().stack().isPresent();
    }

    /**
     * The cards in the layers' place ({@link LayerCards#list}); the bar's controls above them. The cards are laid out
     * only where the place has room: the editor also lays the panels out without any, to measure its header, which
     * would scroll the cards' kept position.
     */
    @Override
    public void layOut(EditorContext editor, EditorLayout layout) {
        if (editor.advanced()) {
            layout.add(EditorRegion.LAYERS_BAR, groundRow(editor, layout.flow(EditorRegion.LAYERS_BAR)));
        }
        Rect place = layout.area(EditorRegion.LAYERS);
        if (place.height() > 0) {
            layout.add(EditorRegion.LAYERS, LayerCards.list(editor, place, cardsScroll));
        }
    }

    @Override
    public boolean shortcut(EditorContext editor, int keyCode, Modifiers modifiers) {
        int selected = editor.shown().selected();
        Runnable action = switch (keyCode) {
            case InputConstants.KEY_UP -> modifiers.alt() ? () -> editor.apply(new LayerEdits.Move(selected + 1))
                    : () -> editor.select(selected + 1);
            case InputConstants.KEY_DOWN -> modifiers.alt() ? () -> editor.apply(new LayerEdits.Move(selected - 1))
                    : () -> editor.select(Math.max(0, selected - 1));
            case InputConstants.KEY_DELETE, InputConstants.KEY_BACKSPACE -> () -> editor.apply(new LayerEdits.Remove());
            case InputConstants.KEY_INSERT -> () -> LayerCards.insert(editor, selected + 1);
            case InputConstants.KEY_D -> modifiers.control() ? () -> editor.apply(new LayerEdits.Duplicate()) : null;
            default -> null;
        };
        if (action == null) {
            return false;
        }
        action.run();
        return true;
    }

    /** The ground's Y to type, with its label: the top layer's thickness changes so the ground is there. */
    private static List<Widget> groundRow(EditorContext editor, FlowLayout bar) {
        Component label = Component.translatable("flatgen.editor.ground_field");
        Rect place = bar.next(Label.width(editor.textWidth(label)));
        return List.of(new Label(Icon.item("minecraft:ladder"), label, place),
                new NumberField("surface", label, bar.next(FIELD),
                        () -> editor.world().floor() + editor.shown().height(),
                        y -> editor.apply(new LayerEdits.SurfaceAt(y))));
    }
}
