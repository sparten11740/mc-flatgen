package dev.flatgen.client.gui;

import dev.flatgen.client.gui.rules.Modifiers;
import dev.flatgen.client.gui.rules.MouseButtons;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.client.gui.rules.WidgetDescription;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import net.minecraft.network.chat.Component;

/**
 * One of a few choices shown one under another, as the templates: a picture of it, its name and a few words on what
 * it is, on a card ringed while it is the one chosen, so which is chosen shows by its shape; a click, Enter or Space
 * chooses it.
 */
public final class ChoiceCard implements Control {
    /** Space inside the card's edges, and between the picture and the words, in GUI pixels. */
    private static final int PADDING = 5;
    private static final int GAP = 6;
    /** The picture's width, in GUI pixels; it is as tall as the card leaves it. */
    private static final int PICTURE_WIDTH = 22;

    private final BiConsumer<Canvas, Rect> picture;
    private final Component name;
    private final List<Component> about;
    private final Rect bounds;
    private final BooleanSupplier chosen;
    private final Runnable choose;

    /**
     * A card of this choice, chosen while {@code chosen} says so, chosen by {@code choose}.
     *
     * @param picture draws the choice's picture in an area
     * @param about   the lines on what it is, already wrapped to {@link #wordsWidth(int)}; as many as fit are drawn,
     *                and the narrator says them all as one
     */
    public ChoiceCard(BiConsumer<Canvas, Rect> picture, Component name, List<Component> about, Rect bounds,
            BooleanSupplier chosen, Runnable choose) {
        this.picture = picture;
        this.name = name;
        this.about = List.copyOf(about);
        this.bounds = bounds;
        this.chosen = chosen;
        this.choose = choose;
    }

    /** How wide the words are on a card this wide, in GUI pixels. */
    public static int wordsWidth(int cardWidth) {
        return cardWidth - 2 * PADDING - PICTURE_WIDTH - GAP;
    }

    /** How tall a card is whose words are this many lines under its name, in GUI pixels. */
    public static int height(int lines) {
        return 2 * PADDING + TextStyle.STRONG.lineHeight() + lines * TextStyle.BODY.lineHeight();
    }

    @Override
    public void draw(Canvas canvas, int mouseX, int mouseY) {
        boolean isChosen = chosen.getAsBoolean();
        canvas.sprite(isChosen ? Look.CHOSEN : bounds.contains(mouseX, mouseY) ? Look.BUTTON_LIT : Look.PANEL,
                bounds);
        picture.accept(canvas, new Rect(bounds.x() + PADDING, bounds.y() + PADDING, PICTURE_WIDTH,
                bounds.height() - 2 * PADDING));
        int x = bounds.x() + PADDING + PICTURE_WIDTH + GAP;
        int y = bounds.y() + PADDING;
        canvas.text(canvas.fitted(name, wordsWidth(bounds.width()), TextStyle.STRONG), x, y, Look.TEXT,
                TextStyle.STRONG);
        y += TextStyle.STRONG.lineHeight();
        for (Component line : about) {
            if (y + TextStyle.BODY.lineHeight() > bounds.bottom() - PADDING) {
                return;
            }
            canvas.text(line, x, y, Look.MUTED);
            y += TextStyle.BODY.lineHeight();
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

    /** Its name, "Selected: …" while chosen, as the game says a chosen entry, then what it is, its lines as one. */
    @Override
    public Optional<Narration> narration() {
        Component said = chosen.getAsBoolean() ? Component.translatable("narrator.select", name) : name;
        String words = String.join(" ", about.stream().map(Component::getString).toList());
        return Optional.of(Narration.button(said, true, List.of(Component.literal(words))));
    }

    @Override
    public Rect bounds() {
        return bounds;
    }

    @Override
    public List<WidgetDescription> describe() {
        return List.of(new WidgetDescription("tab", name.getString(), bounds, true,
                chosen.getAsBoolean() ? "chosen" : ""));
    }
}
