package dev.flatgen.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import dev.flatgen.client.gui.rules.Modifiers;
import dev.flatgen.client.gui.rules.MouseButtons;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.client.gui.rules.TypedText;
import dev.flatgen.client.gui.rules.WidgetDescription;
import java.util.List;
import java.util.Optional;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import net.minecraft.network.chat.Component;

/**
 * A box holding a whole number. It shows the number it is given; clicked or given the keyboard's focus, it takes the
 * keyboard and the typed digits replace it, and Enter hands the new number on, while Escape, which drops what was
 * typed, or a click elsewhere leave it as it was.
 */
public final class NumberField implements Control {
    /** The most characters typed, enough for any Y or height with its sign. */
    private static final int MOST_CHARACTERS = 6;

    private final String name;
    private final Component label;
    private final Rect bounds;
    private final IntSupplier value;
    private final IntConsumer onEnter;
    private final TypedText typed = new TypedText(text -> text.matches("-?\\d*"), MOST_CHARACTERS);
    private boolean keyboard;
    /** Whether it is drawn square and flat, filling its place in a box it shares, as between a stepper's buttons. */
    private boolean flat;
    /** What it does when the keyboard's focus comes to it, such as selecting the layer its card shows. */
    private Runnable onFocus = () -> { };

    /**
     * A box showing a number from {@code value}, handing a typed one to {@code onEnter}.
     *
     * @param name  what it is called, for tools that drive the screen
     * @param label what it is called, for the player, as the narrator says it
     */
    public NumberField(String name, Component label, Rect bounds, IntSupplier value, IntConsumer onEnter) {
        this.name = name;
        this.label = label;
        this.bounds = bounds;
        this.value = value;
        this.onEnter = onEnter;
    }

    /** The field drawn square and flat, filling its place in a box it shares, as between a stepper's buttons. */
    public NumberField flat() {
        this.flat = true;
        return this;
    }

    /** The field doing this as well when the keyboard's focus comes to it. */
    public NumberField onFocus(Runnable action) {
        this.onFocus = action;
        return this;
    }

    @Override
    public void draw(Canvas canvas, int mouseX, int mouseY) {
        String text = keyboard ? typed.text() + "_" : shown();
        if (flat) {
            FieldLook.drawFlat(canvas, bounds, keyboard, Component.literal(text), Look.TEXT);
        } else {
            FieldLook.drawCentered(canvas, bounds, keyboard, Component.literal(text), Look.TEXT);
        }
    }

    @Override
    public boolean click(double x, double y, int button, Modifiers modifiers) {
        return button == MouseButtons.LEFT && bounds.contains(x, y);
    }

    @Override
    public boolean takesKeyboard() {
        return true;
    }

    @Override
    public boolean focused() {
        return keyboard;
    }

    @Override
    public void keyboard(boolean held) {
        if (held != keyboard) {
            keyboard = held;
            typed.clear();
        }
        if (held) {
            onFocus.run();
        }
    }

    @Override
    public boolean key(int keyCode, Modifiers modifiers) {
        if (keyCode == InputConstants.KEY_RETURN || keyCode == InputConstants.KEY_NUMPADENTER) {
            enter();
            return true;
        }
        if (keyCode == InputConstants.KEY_BACKSPACE) {
            typed.backspace();
            return true;
        }
        if (keyCode == InputConstants.KEY_ESCAPE && !typed.text().isEmpty()) {
            typed.clear();
            return true;
        }
        return keyCode == InputConstants.KEY_DELETE;
    }

    @Override
    public boolean type(char character) {
        return typed.type(character);
    }

    @Override
    public Optional<Narration> narration() {
        return Optional.of(Narration.field(label, keyboard ? typed.text() : shown()));
    }

    /** The number it shows while not being typed in. */
    private String shown() {
        return Integer.toString(value.getAsInt());
    }

    @Override
    public Rect bounds() {
        return bounds;
    }

    @Override
    public List<WidgetDescription> describe() {
        return List.of(new WidgetDescription("number", name, bounds, true, Integer.toString(value.getAsInt())));
    }

    private void enter() {
        String text = typed.text();
        if (!text.isEmpty() && !text.equals("-")) {
            onEnter.accept(Integer.parseInt(text));
        }
        typed.clear();
    }
}
