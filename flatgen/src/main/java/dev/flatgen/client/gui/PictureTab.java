package dev.flatgen.client.gui;

import dev.flatgen.client.gui.rules.Modifiers;
import dev.flatgen.client.gui.rules.MouseButtons;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.client.gui.rules.WidgetDescription;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;

/**
 * A tab showing a picture, standing on what it shows as the creative inventory's tabs do: the chosen one is taller and
 * the others stand behind it, so which is chosen shows by its shape, not by colour alone; its tooltip says what it is.
 */
public final class PictureTab implements Control {
    /** A tab's height, in GUI pixels: a picture with room around it. */
    public static final int HEIGHT = Icon.SIZE + 6;
    /** How much lower a tab that is not chosen starts than the chosen one, in GUI pixels. */
    private static final int LOWERED = 3;
    /** The shade over a tab that is not chosen, which stands behind the chosen one. */
    private static final int BEHIND = 0x60000000;

    private final Icon icon;
    private final Rect bounds;
    private final BooleanSupplier chosen;
    private final Runnable choose;
    private final Supplier<List<Component>> tooltip;

    /** A tab showing this picture in this rectangle, chosen while {@code chosen} says so, doing {@code choose}. */
    public PictureTab(Icon icon, Rect bounds, BooleanSupplier chosen, Runnable choose,
            Supplier<List<Component>> tooltip) {
        this.icon = icon;
        this.bounds = bounds;
        this.chosen = chosen;
        this.choose = choose;
        this.tooltip = tooltip;
    }

    @Override
    public void draw(Canvas canvas, int mouseX, int mouseY) {
        boolean isChosen = chosen.getAsBoolean();
        Rect frame = isChosen ? bounds
                : new Rect(bounds.x(), bounds.y() + LOWERED, bounds.width(), bounds.height() - LOWERED);
        boolean hovered = bounds.contains(mouseX, mouseY);
        canvas.button(frame, hovered, true);
        if (!isChosen) {
            canvas.fill(frame, BEHIND);
        }
        icon.draw(canvas, frame.x() + (frame.width() - Icon.SIZE) / 2, frame.y() + (frame.height() - Icon.SIZE) / 2);
        if (hovered) {
            canvas.tooltip(tooltip.get(), mouseX, mouseY);
        }
    }

    @Override
    public boolean click(double x, double y, int button, Modifiers modifiers) {
        return button == MouseButtons.LEFT && bounds.contains(x, y) && Control.use(() -> true, choose);
    }

    /** Enter or Space chooses it while it has the focus, as a click does. */
    @Override
    public boolean key(int keyCode, Modifiers modifiers) {
        return Keys.uses(keyCode) && Control.use(() -> true, choose);
    }

    @Override
    public List<Component> tooltip() {
        return tooltip.get();
    }

    /** Its name, "Selected: …" while chosen, as the game says a chosen entry, and the rest of its tooltip. */
    @Override
    public Optional<Narration> narration() {
        List<Component> lines = tooltip.get();
        Component name = Narration.name(lines);
        return Optional.of(Narration.button(chosen.getAsBoolean() ? Component.translatable("narrator.select", name)
                : name, true, Narration.more(lines)));
    }

    @Override
    public Rect bounds() {
        return bounds;
    }

    @Override
    public List<WidgetDescription> describe() {
        return List.of(new WidgetDescription("tab", Narration.name(tooltip.get()).getString(), bounds, true,
                chosen.getAsBoolean() ? "chosen" : ""));
    }
}
