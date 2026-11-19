package dev.flatgen.editor.client.panel;

import dev.flatgen.client.gui.Icon;
import dev.flatgen.client.gui.Surface;
import dev.flatgen.client.gui.TextButton;
import dev.flatgen.client.gui.Widget;
import dev.flatgen.client.gui.rules.FlowLayout;
import dev.flatgen.client.gui.rules.Rect;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;
import net.minecraft.network.chat.Component;

/**
 * Where the editor's panels place their widgets: each {@link EditorRegion}'s area, a flow filling it from the top left
 * in the order the panels come, and the widgets placed in it so far, which the editor then draws there.
 */
public final class EditorLayout {
    private final Map<EditorRegion, Rect> areas;
    /** How wide body text is drawn, for buttons as wide as their names. */
    private final ToIntFunction<Component> textWidth;
    private final Map<EditorRegion, FlowLayout> flows = new EnumMap<>(EditorRegion.class);
    private final Map<EditorRegion, List<Widget>> widgets = new EnumMap<>(EditorRegion.class);

    /** A layout of the regions in these areas, every region having one, perhaps empty, measuring text this way. */
    public EditorLayout(Map<EditorRegion, Rect> areas, ToIntFunction<Component> textWidth) {
        this.areas = new EnumMap<>(areas);
        this.textWidth = textWidth;
        for (EditorRegion region : EditorRegion.values()) {
            Rect area = this.areas.get(region);
            flows.put(region, new FlowLayout(area, region.rowHeight(), PanelSizes.GAP));
            widgets.put(region, new ArrayList<>());
        }
    }

    /** A region's area. */
    public Rect area(EditorRegion region) {
        return areas.get(region);
    }

    /** The flow placing the next widgets of a region, below or after those placed before. */
    public FlowLayout flow(EditorRegion region) {
        return flows.get(region);
    }

    /** Adds widgets to a region, drawn in the order added. */
    public void add(EditorRegion region, List<? extends Widget> placed) {
        widgets.get(region).addAll(placed);
    }

    /** Adds a widget to a region. */
    public void add(EditorRegion region, Widget placed) {
        widgets.get(region).add(placed);
    }

    /**
     * Adds a button with a picture and its name to a region, after what is there, as wide as the name needs, or only
     * its picture, square, when the rest of the row has no room for the name; its tooltip is its name, then what
     * {@code more} says. Returns it, to be drawn as another kind.
     */
    public TextButton button(EditorRegion region, Icon icon, Component name, Runnable action, BooleanSupplier usable,
            Supplier<List<Component>> more) {
        FlowLayout flow = flow(region);
        int named = TextButton.width(textWidth.applyAsInt(name));
        int width = flow.rest().width() >= named ? named : region.rowHeight();
        TextButton button = new TextButton(icon, () -> name, flow.next(width), action, usable,
                () -> Stream.concat(Stream.of(name), more.get().stream()).toList());
        add(region, button);
        return button;
    }

    /** Adds a box to the aside, on a raised panel, holding what the content places in the flow it is given. */
    public void box(Function<FlowLayout, List<Widget>> content) {
        add(EditorRegion.ASIDE, Surface.box(flow(EditorRegion.ASIDE), content));
    }

    /** The widgets placed in a region. */
    public List<Widget> widgets(EditorRegion region) {
        return List.copyOf(widgets.get(region));
    }
}
