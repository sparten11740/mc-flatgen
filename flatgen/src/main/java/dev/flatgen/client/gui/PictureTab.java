package dev.flatgen.client.gui;

import dev.flatgen.client.gui.rules.Modifiers;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.client.gui.rules.WidgetDescription;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;

/**
 * A tab of a segmented choice, such as the dimensions, or a picker's shelf: a picture and its name. The chosen one
 * stands on a blue fill ({@link Look#TAB_CHOSEN}) with its name light blue and in bold, so which is chosen shows by its
 * fill and its weight, not by colour alone, and never looks like the grey one under the mouse; its tooltip says more.
 */
public final class PictureTab implements Control {
    /** A tab's height, in GUI pixels: its picture's. */
    public static final int HEIGHT = Icon.SIZE;
    /** Space at the tab's sides and between its picture and its name, in GUI pixels. */
    private static final int PADDING = 6;
    private static final int GAP = 4;

    private final Icon icon;
    private final Supplier<Component> name;
    private final Rect bounds;
    private final BooleanSupplier chosen;
    private final Runnable choose;
    private final Supplier<List<Component>> tooltip;

    /** A tab showing this picture and name in this rectangle, chosen while {@code chosen} says so. */
    public PictureTab(Icon icon, Supplier<Component> name, Rect bounds, BooleanSupplier chosen, Runnable choose,
            Supplier<List<Component>> tooltip) {
        this.icon = icon;
        this.name = name;
        this.bounds = bounds;
        this.chosen = chosen;
        this.choose = choose;
        this.tooltip = tooltip;
    }

    /** How wide a tab is whose name is this wide, in GUI pixels. */
    public static int width(int nameWidth) {
        return 2 * PADDING + Icon.SIZE + GAP + nameWidth;
    }

    @Override
    public void draw(Canvas canvas, int mouseX, int mouseY) {
        boolean isChosen = chosen.getAsBoolean();
        boolean hovered = bounds.contains(mouseX, mouseY);
        if (isChosen || hovered) {
            canvas.sprite(isChosen ? Look.TAB_CHOSEN : Look.BUTTON, bounds);
        }
        icon.draw(canvas, bounds.x() + PADDING, bounds.y() + (bounds.height() - Icon.SIZE) / 2);
        canvas.text(name.get(), bounds.x() + PADDING + Icon.SIZE + GAP, bounds.y() + (bounds.height()
                - TextStyle.BODY.lineHeight()) / 2 + 1, isChosen ? Look.ON_ACCENT : Look.MUTED,
                isChosen ? TextStyle.STRONG : TextStyle.BODY);
        if (hovered) {
            canvas.tooltipBelow(tooltip.get(), bounds);
        }
    }

    @Override
    public boolean click(double x, double y, int button, Modifiers modifiers) {
        return Control.clicked(bounds, x, y, button, () -> true, choose);
    }

    /** Enter or Space chooses it while it has the focus, as a click does. */
    @Override
    public boolean key(int keyCode, Modifiers modifiers) {
        return Control.keyed(keyCode, () -> true, choose);
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
