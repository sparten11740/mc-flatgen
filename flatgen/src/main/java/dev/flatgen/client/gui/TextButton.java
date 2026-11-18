package dev.flatgen.client.gui;

import dev.flatgen.client.gui.rules.Modifiers;
import dev.flatgen.client.gui.rules.MouseButtons;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.client.gui.rules.WidgetDescription;
import dev.flatgen.mc.UiSounds;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;

/**
 * A button with a label that does something when clicked, drawn and sounding as the game's own buttons do. It can be
 * unusable for a while, drawn so, with a tooltip saying why.
 */
public final class TextButton implements Widget {
    private static final int LABEL_COLOR = 0xFFFFFFFF;
    private static final int DISABLED_LABEL_COLOR = 0xFFA0A0A0;

    private final Supplier<Component> label;
    private final Rect bounds;
    private final Runnable action;
    private final BooleanSupplier active;
    private final Supplier<List<Component>> tooltip;

    /** A button always usable, without a tooltip. */
    public TextButton(Component label, Rect bounds, Runnable action) {
        this(() -> label, bounds, action, () -> true, List::of);
    }

    /**
     * A button usable while {@code active} says so, with a tooltip whose lines may change, empty for none.
     */
    public TextButton(Component label, Rect bounds, Runnable action, BooleanSupplier active,
            Supplier<List<Component>> tooltip) {
        this(() -> label, bounds, action, active, tooltip);
    }

    /** A button whose label may change, usable while {@code active} says so, with a tooltip, empty for none. */
    public TextButton(Supplier<Component> label, Rect bounds, Runnable action, BooleanSupplier active,
            Supplier<List<Component>> tooltip) {
        this.label = label;
        this.bounds = bounds;
        this.action = action;
        this.active = active;
        this.tooltip = tooltip;
    }

    @Override
    public void draw(Canvas canvas, int mouseX, int mouseY) {
        boolean usable = active.getAsBoolean();
        boolean hovered = bounds.contains(mouseX, mouseY);
        canvas.button(bounds, hovered, usable);
        canvas.centeredText(label.get(), bounds, usable ? LABEL_COLOR : DISABLED_LABEL_COLOR);
        List<Component> lines = tooltip.get();
        if (hovered && !lines.isEmpty()) {
            canvas.tooltip(lines, mouseX, mouseY);
        }
    }

    @Override
    public boolean click(double x, double y, int button, Modifiers modifiers) {
        if (button != MouseButtons.LEFT || !bounds.contains(x, y)) {
            return false;
        }
        if (active.getAsBoolean()) {
            UiSounds.click();
            action.run();
        }
        return true;
    }

    @Override
    public Optional<WidgetDescription> describe() {
        String text = label.get().getString().isEmpty() && !tooltip.get().isEmpty()
                ? tooltip.get().get(0).getString() : label.get().getString();
        return Optional.of(new WidgetDescription("button", text, bounds, active.getAsBoolean(), ""));
    }
}
