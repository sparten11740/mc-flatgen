package dev.flatgen.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import dev.flatgen.client.gui.rules.Modifiers;
import dev.flatgen.client.gui.rules.MouseButtons;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.client.gui.rules.TypedText;
import dev.flatgen.client.gui.rules.WidgetDescription;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;

/** A box to type text into, such as a search, which hands on the text as it changes; empty, it shows a hint. */
public final class TextField implements Control {
    /** The most characters typed. */
    private static final int MOST_CHARACTERS = 64;
    /** The first character a player can type; those below are controls. */
    private static final char FIRST_TYPED = ' ';
    /** The picture before a search, a spyglass, and the space between it and the field, in GUI pixels. */
    private static final Icon SEARCH = Icon.item("minecraft:spyglass");
    private static final int SEARCH_GAP = 3;

    private final String name;
    private final Rect bounds;
    private final Component hint;
    private final Consumer<String> onChange;
    private final TypedText typed =
            new TypedText(text -> text.chars().allMatch(character -> character >= FIRST_TYPED), MOST_CHARACTERS);
    private boolean keyboard;

    /** The width a search takes with a field this wide, in GUI pixels: the spyglass and the space before the field. */
    public static int searchWidth(int fieldWidth) {
        return Icon.SIZE + SEARCH_GAP + fieldWidth;
    }

    /** A search: the spyglass, then a field filling the rest of this place, handing its text to {@code onChange}. */
    public static Widget search(Rect place, Component hint, String text, Consumer<String> onChange) {
        Rect glass = new Rect(place.x(), place.y(), Icon.SIZE, place.height());
        int fieldX = glass.right() + SEARCH_GAP;
        return new WidgetGroup(List.of(new Label(SEARCH, Component.empty(), glass),
                new TextField("search", new Rect(fieldX, place.y(), place.right() - fieldX, place.height()), hint, text,
                        onChange)));
    }

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

    /** Shows this text in place of what was typed, as when the player picks something to name. */
    public void show(String text) {
        typed.clear();
        text.chars().forEach(character -> typed.type((char) character));
        onChange.accept(typed.text());
    }

    @Override
    public void draw(Canvas canvas, int mouseX, int mouseY) {
        String text = typed.text() + (keyboard ? "_" : "");
        boolean showHint = text.isEmpty();
        FieldLook.draw(canvas, bounds, keyboard, showHint ? hint : Component.literal(text),
                showHint ? Look.MUTED : Look.TEXT);
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
    public void keyboard(boolean held) {
        keyboard = held;
    }

    @Override
    public boolean key(int keyCode, Modifiers modifiers) {
        if (keyCode != InputConstants.KEY_BACKSPACE) {
            return keyCode == InputConstants.KEY_DELETE;
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
    public Optional<Narration> narration() {
        return Optional.of(Narration.field(hint, typed.text()));
    }

    @Override
    public Rect bounds() {
        return bounds;
    }

    @Override
    public List<WidgetDescription> describe() {
        return List.of(new WidgetDescription("text", name, bounds, true, typed.text()));
    }
}
