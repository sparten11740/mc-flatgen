package dev.flatgen.client.gui;

import dev.flatgen.client.gui.rules.Modifiers;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.client.gui.rules.TypedText;
import dev.flatgen.client.gui.rules.WidgetDescription;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/** A box to type text into, such as a search, which hands on the text as it changes; empty, it shows a hint. */
public final class TextField implements Widget {
    /** The most characters typed. */
    private static final int MOST_CHARACTERS = 64;
    /** The first character a player can type; those below are controls. */
    private static final char FIRST_TYPED = ' ';
    private static final int LEFT_BUTTON = 0;

    private final String name;
    private final Rect bounds;
    private final Component hint;
    private final Consumer<String> onChange;
    private final TypedText typed =
            new TypedText(text -> text.chars().allMatch(character -> character >= FIRST_TYPED), MOST_CHARACTERS);
    private boolean keyboard;

    /**
     * A box with a hint, starting with some text, handing its text to {@code onChange} each time it changes.
     *
     * @param name what it is called, for tools that drive the screen
     */
    public TextField(String name, Rect bounds, Component hint, String text, Consumer<String> onChange) {
        this.name = name;
        this.bounds = bounds;
        this.hint = hint;
        this.onChange = onChange;
        text.chars().forEach(character -> typed.type((char) character));
    }

    @Override
    public void draw(Canvas canvas, int mouseX, int mouseY) {
        String text = typed.text() + (keyboard ? "_" : "");
        boolean showHint = text.isEmpty();
        FieldLook.draw(canvas, bounds, keyboard, showHint ? hint : Component.literal(text),
                showHint ? FieldLook.HINT_COLOR : FieldLook.TEXT_COLOR);
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
        keyboard = held;
    }

    @Override
    public boolean key(int keyCode, Modifiers modifiers) {
        if (keyCode != GLFW.GLFW_KEY_BACKSPACE) {
            return keyCode == GLFW.GLFW_KEY_DELETE;
        }
        typed.backspace();
        onChange.accept(typed.text());
        return true;
    }

    @Override
    public boolean type(char character) {
        if (!typed.type(character)) {
            return false;
        }
        onChange.accept(typed.text());
        return true;
    }

    @Override
    public Optional<WidgetDescription> describe() {
        return Optional.of(new WidgetDescription("text", name, bounds, true, typed.text()));
    }
}
