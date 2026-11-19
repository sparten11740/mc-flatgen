package dev.flatgen.client.gui;

import dev.flatgen.client.gui.rules.Modifiers;
import dev.flatgen.client.gui.rules.MouseButtons;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.client.gui.rules.WidgetDescription;
import java.util.List;
import java.util.Optional;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;

/**
 * A whole number from a range, set by pressing or dragging along it or by turning the wheel over it (one a notch, ten
 * with Shift), with its name and value written on it and the track filled up to its knob.
 */
public final class Slider implements Control {
    /** The width of the handle, in GUI pixels. */
    private static final int HANDLE = 8;
    /** How far the handle's knob sits inside the slider's top and bottom, in GUI pixels. */
    private static final int KNOB_INSET = 2;
    /** How far a notch of the wheel moves the value, alone and with Shift. */
    private static final int WHEEL_STEP = 1;
    private static final int SHIFT_WHEEL_STEP = 10;

    private final String name;
    private final Rect bounds;
    private final int min;
    private final int max;
    private final IntSupplier value;
    private final IntConsumer onChange;
    private final Supplier<Component> label;

    /**
     * A slider from {@code min} to {@code max} showing {@code value}, handing each new value to {@code onChange}.
     *
     * @param name  what it is called, for tools that drive the screen
     * @param label what is written on it, its name and value
     */
    public Slider(String name, Rect bounds, int min, int max, IntSupplier value, IntConsumer onChange,
            Supplier<Component> label) {
        this.name = name;
        this.bounds = bounds;
        this.min = min;
        this.max = max;
        this.value = value;
        this.onChange = onChange;
        this.label = label;
    }

    @Override
    public void draw(Canvas canvas, int mouseX, int mouseY) {
        canvas.field(bounds, bounds.contains(mouseX, mouseY));
        int travel = bounds.width() - HANDLE;
        int handleX = bounds.x() + (max == min ? 0 : (value.getAsInt() - min) * travel / (max - min));
        canvas.sprite(Look.button(Look.ButtonKind.ACCENT, false, true), new Rect(bounds.x(), bounds.y(),
                handleX + HANDLE - bounds.x(), bounds.height()));
        canvas.sprite(Look.KNOB, new Rect(handleX, bounds.y() + KNOB_INSET, HANDLE, bounds.height() - 2 * KNOB_INSET));
        canvas.centeredText(label.get(), bounds, Look.TEXT);
    }

    @Override
    public boolean click(double x, double y, int button, Modifiers modifiers) {
        if (button != MouseButtons.LEFT || !bounds.contains(x, y)) {
            return false;
        }
        setFrom(x);
        return true;
    }

    @Override
    public void drag(double x, double y) {
        setFrom(x);
    }

    @Override
    public boolean scroll(double x, double y, double amount, Modifiers modifiers) {
        if (!bounds.contains(x, y)) {
            return false;
        }
        move((int) Math.signum(amount), modifiers);
        return true;
    }

    /** Left and Right move it while it has the focus, as the wheel does; Up and Down move the focus on. */
    @Override
    public boolean key(int keyCode, Modifiers modifiers) {
        int direction = Keys.across(keyCode);
        if (direction != 0) {
            move(direction, modifiers);
        }
        return direction != 0;
    }

    /** Moves the value a step up or down, ten with Shift. */
    private void move(int direction, Modifiers modifiers) {
        int step = modifiers.shift() ? SHIFT_WHEEL_STEP : WHEEL_STEP;
        onChange.accept(within(value.getAsInt() + direction * step));
    }

    @Override
    public Optional<Narration> narration() {
        return Optional.of(Narration.slider(label.get()));
    }

    @Override
    public Rect bounds() {
        return bounds;
    }

    @Override
    public List<WidgetDescription> describe() {
        return List.of(new WidgetDescription("slider", name, bounds, true, Integer.toString(value.getAsInt())));
    }

    /** Sets the value the pointer at this X stands for. */
    private void setFrom(double x) {
        double share = (x - bounds.x() - HANDLE / 2.0) / (bounds.width() - HANDLE);
        onChange.accept(within(min + (int) Math.round(share * (max - min))));
    }

    private int within(int number) {
        return Math.max(min, Math.min(max, number));
    }
}
