package dev.flatgen.client.gui;

import dev.flatgen.client.gui.rules.Modifiers;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.client.gui.rules.WidgetDescription;
import java.util.Optional;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/**
 * A box holding a whole number. It shows the number it is given; clicked, it takes the keyboard and the typed digits
 * replace it, and Enter hands the new number on, while Escape or a click elsewhere, which take the keyboard away,
 * leave it as it was.
 */
public final class NumberField implements Widget {
    private static final String SPRITE = "minecraft:widget/text_field";
    private static final String FOCUSED_SPRITE = "minecraft:widget/text_field_highlighted";
    private static final int TEXT_COLOR = 0xFFE0E0E0;
    /** Space between the box's edge and its number, in GUI pixels. */
    private static final int PADDING = 4;
    /** The most characters typed, enough for any Y or height with its sign. */
    private static final int MOST_CHARACTERS = 6;
    private static final int LEFT_BUTTON = 0;

    private final String name;
    private final Rect bounds;
    private final IntSupplier value;
    private final IntConsumer onEnter;
    private String typed;

    /**
     * A box showing a number from {@code value}, handing a typed one to {@code onEnter}.
     *
     * @param name what it is called, for tools that drive the screen
     */
    public NumberField(String name, Rect bounds, IntSupplier value, IntConsumer onEnter) {
        this.name = name;
        this.bounds = bounds;
        this.value = value;
        this.onEnter = onEnter;
    }

    @Override
    public void draw(Canvas canvas, int mouseX, int mouseY) {
        canvas.sprite(typed == null ? SPRITE : FOCUSED_SPRITE, bounds);
        String text = typed == null ? Integer.toString(value.getAsInt()) : typed + "_";
        canvas.text(Component.literal(text), bounds.x() + PADDING,
                bounds.y() + (bounds.height() - canvas.lineHeight()) / 2 + 1, TEXT_COLOR);
    }

    @Override
    public boolean click(double x, double y, int button, Modifiers modifiers) {
        return button == LEFT_BUTTON && bounds.contains(x, y);
    }

    @Override
    public boolean takesKeyboard() {
        return true;
    }

    @Override
    public void keyboard(boolean held) {
        typed = held ? "" : null;
    }

    @Override
    public boolean key(int keyCode, Modifiers modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            enter();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            typed = typed.isEmpty() ? typed : typed.substring(0, typed.length() - 1);
            return true;
        }
        return keyCode == GLFW.GLFW_KEY_DELETE;
    }

    @Override
    public boolean type(char character) {
        boolean sign = character == '-' && typed.isEmpty();
        if ((Character.isDigit(character) || sign) && typed.length() < MOST_CHARACTERS) {
            typed += character;
            return true;
        }
        return false;
    }

    @Override
    public Optional<WidgetDescription> describe() {
        return Optional.of(new WidgetDescription("number", name, bounds, true, Integer.toString(value.getAsInt())));
    }

    private void enter() {
        if (!typed.isEmpty() && !typed.equals("-")) {
            onEnter.accept(Integer.parseInt(typed));
        }
        typed = "";
    }
}
