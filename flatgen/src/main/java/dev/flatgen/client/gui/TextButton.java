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
 * A button with a label that does something when clicked, drawn and sounding as the game's own buttons do, with a
 * picture before the label that says the same to a player who cannot read. It can be unusable for a while, drawn so,
 * with a tooltip saying why.
 */
public final class TextButton implements Control {
    /** Space between the button's edge and its picture, and between the picture and the label, in GUI pixels. */
    private static final int PICTURE_GAP = 3;
    /** Space at each side of the label, in GUI pixels. */
    private static final int LABEL_PADDING = 6;

    private final Optional<Icon> icon;
    private final Supplier<Component> label;
    private final Rect bounds;
    private final Runnable action;
    private final BooleanSupplier active;
    private final Supplier<List<Component>> tooltip;
    /** What the button does, which its colour says. */
    private Look.ButtonKind kind = Look.ButtonKind.PLAIN;

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
        this(Optional.empty(), label, bounds, action, active, tooltip);
    }

    /** A button with a picture before its label, which may change, usable while {@code active} says so. */
    public TextButton(Icon icon, Supplier<Component> label, Rect bounds, Runnable action, BooleanSupplier active,
            Supplier<List<Component>> tooltip) {
        this(Optional.of(icon), label, bounds, action, active, tooltip);
    }

    /** A button with a picture before its label, always usable, without a tooltip. */
    public TextButton(Icon icon, Component label, Rect bounds, Runnable action) {
        this(Optional.of(icon), () -> label, bounds, action, () -> true, List::of);
    }

    private TextButton(Optional<Icon> icon, Supplier<Component> label, Rect bounds, Runnable action,
            BooleanSupplier active, Supplier<List<Component>> tooltip) {
        this.icon = icon;
        this.label = label;
        this.bounds = bounds;
        this.action = action;
        this.active = active;
        this.tooltip = tooltip;
    }

    /** How wide a button with a picture is whose label is this wide, in GUI pixels. */
    public static int width(int labelWidth) {
        return PICTURE_GAP + Icon.SIZE + labelWidth + 2 * LABEL_PADDING;
    }

    /** The button drawn as one of the look's kinds, such as the green one that goes ahead. */
    public TextButton kind(Look.ButtonKind buttonKind) {
        this.kind = buttonKind;
        return this;
    }

    @Override
    public void draw(Canvas canvas, int mouseX, int mouseY) {
        boolean usable = active.getAsBoolean();
        boolean hovered = bounds.contains(mouseX, mouseY);
        canvas.sprite(Look.button(kind, hovered, usable), bounds);
        int labelX = bounds.x();
        if (icon.isPresent()) {
            Rect picture = new Rect(bounds.x() + PICTURE_GAP, bounds.y() + (bounds.height() - Icon.SIZE) / 2,
                    Icon.SIZE, Icon.SIZE);
            icon.get().draw(canvas, picture.x(), picture.y());
            dimUnless(usable, canvas, picture);
            labelX = picture.right();
        }
        Rect labelArea = new Rect(labelX, bounds.y(), bounds.right() - labelX, bounds.height());
        canvas.centeredText(label.get(), labelArea, usable ? kind.label() : Look.DISABLED);
        List<Component> lines = tooltip.get();
        if (hovered && !lines.isEmpty()) {
            canvas.tooltip(lines, mouseX, mouseY);
        }
    }

    private static void dimUnless(boolean usable, Canvas canvas, Rect picture) {
        if (!usable) {
            canvas.dim(picture);
        }
    }

    @Override
    public boolean click(double x, double y, int button, Modifiers modifiers) {
        return button == MouseButtons.LEFT && bounds.contains(x, y) && Control.use(active, action);
    }

    /** Enter or Space uses it while it has the focus, as a click does. */
    @Override
    public boolean key(int keyCode, Modifiers modifiers) {
        return Keys.uses(keyCode) && Control.use(active, action);
    }

    @Override
    public List<Component> tooltip() {
        return tooltip.get();
    }

    @Override
    public Optional<Narration> narration() {
        Component name = label.get();
        return Optional.of(name.getString().isEmpty() ? Narration.button(tooltip.get(), active.getAsBoolean())
                : Narration.button(name, active.getAsBoolean(), tooltip.get()));
    }

    @Override
    public Rect bounds() {
        return bounds;
    }

    @Override
    public List<WidgetDescription> describe() {
        String text = label.get().getString().isEmpty() && !tooltip.get().isEmpty()
                ? tooltip.get().get(0).getString() : label.get().getString();
        return List.of(new WidgetDescription("button", text, bounds, active.getAsBoolean(), ""));
    }
}
