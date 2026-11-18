package dev.flatgen.client.gui;

import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.mc.UiSounds;
import net.minecraft.network.chat.Component;

/** A button with a label that does something when clicked, drawn and sounding as the game's own buttons do. */
public final class TextButton implements Widget {
    private static final String SPRITE = "minecraft:widget/button";
    private static final String HOVERED_SPRITE = "minecraft:widget/button_highlighted";
    /** White, the colour of a button's label. */
    private static final int LABEL_COLOR = 0xFFFFFFFF;
    /** The left mouse button. */
    private static final int LEFT_BUTTON = 0;

    private final Component label;
    private final Rect bounds;
    private final Runnable action;

    public TextButton(Component label, Rect bounds, Runnable action) {
        this.label = label;
        this.bounds = bounds;
        this.action = action;
    }

    @Override
    public void draw(Canvas canvas, int mouseX, int mouseY) {
        canvas.sprite(bounds.contains(mouseX, mouseY) ? HOVERED_SPRITE : SPRITE, bounds);
        canvas.centeredText(label, bounds, LABEL_COLOR);
    }

    @Override
    public boolean click(double x, double y, int button) {
        if (button != LEFT_BUTTON || !bounds.contains(x, y)) {
            return false;
        }
        UiSounds.click();
        action.run();
        return true;
    }
}
