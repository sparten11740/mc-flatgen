package dev.flatgen.client.gui;

import dev.flatgen.client.gui.rules.Modifiers;
import dev.flatgen.client.gui.rules.MouseButtons;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.client.gui.rules.WidgetDescription;
import java.util.List;
import java.util.Optional;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;

/**
 * A button stepping through a few states when clicked, showing the picture of the state it is in and, beside it, a pip
 * for each state, the one it is in filled and the others hollow, so how many there are and which is on show without
 * words or colour; its tooltip names the state.
 */
public final class CycleButton implements Control {
    /** The border around the picture, and the size of a pip and the space before it, in GUI pixels. */
    private static final int BORDER = 2;
    private static final int PIP = 4;
    private static final int PIP_GAP = 2;

    private final List<Icon> states;
    private final IntSupplier current;
    private final Rect bounds;
    private final Runnable next;
    private final Supplier<List<Component>> tooltip;

    /**
     * A button showing these states' pictures, in this rectangle, as wide as {@link #width} says.
     *
     * @param current which state it is in, by its index
     * @param next    moves to the next state
     * @param tooltip the state's name, and more if needed
     */
    public CycleButton(List<Icon> states, IntSupplier current, Rect bounds, Runnable next,
            Supplier<List<Component>> tooltip) {
        this.states = List.copyOf(states);
        this.current = current;
        this.bounds = bounds;
        this.next = next;
        this.tooltip = tooltip;
    }

    /** How wide a button of this many states is, in GUI pixels: its picture and its pips. */
    public static int width(int states) {
        return 2 * BORDER + Icon.SIZE + states * (PIP_GAP + PIP);
    }

    @Override
    public void draw(Canvas canvas, int mouseX, int mouseY) {
        boolean hovered = bounds.contains(mouseX, mouseY);
        canvas.button(bounds, hovered, true);
        int on = current.getAsInt();
        states.get(on).draw(canvas, bounds.x() + BORDER, bounds.y() + (bounds.height() - Icon.SIZE) / 2);
        int pipY = bounds.y() + (bounds.height() - PIP) / 2;
        for (int state = 0; state < states.size(); state++) {
            Rect pip = new Rect(bounds.x() + BORDER + Icon.SIZE + PIP_GAP + state * (PIP_GAP + PIP), pipY, PIP, PIP);
            if (state == on) {
                canvas.fill(pip, Look.TEXT);
            } else {
                canvas.outline(pip, Look.TEXT);
            }
        }
        if (hovered) {
            canvas.tooltipBelow(tooltip.get(), bounds);
        }
    }

    @Override
    public boolean click(double x, double y, int button, Modifiers modifiers) {
        return button == MouseButtons.LEFT && bounds.contains(x, y) && Control.use(() -> true, next);
    }

    /** Enter or Space uses it while it has the focus, as a click does. */
    @Override
    public boolean key(int keyCode, Modifiers modifiers) {
        return Keys.uses(keyCode) && Control.use(() -> true, next);
    }

    @Override
    public List<Component> tooltip() {
        return tooltip.get();
    }

    @Override
    public Optional<Narration> narration() {
        return Optional.of(Narration.button(tooltip.get(), true));
    }

    @Override
    public Rect bounds() {
        return bounds;
    }

    @Override
    public List<WidgetDescription> describe() {
        List<Component> lines = tooltip.get();
        return List.of(new WidgetDescription("button", Narration.name(lines).getString(), bounds, true,
                Integer.toString(current.getAsInt())));
    }
}
