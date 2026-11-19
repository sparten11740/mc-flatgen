package dev.flatgen.client.gui;

import dev.flatgen.client.gui.rules.Rect;

/**
 * A sprite drawn behind other widgets, such as the raised panel a group of controls sits on; it takes no input.
 *
 * @param sprite the sprite's id, one of {@link Look}'s
 * @param bounds where it is drawn
 */
public record Surface(String sprite, Rect bounds) implements Widget {
    @Override
    public void draw(Canvas canvas, int mouseX, int mouseY) {
        canvas.sprite(sprite, bounds);
    }
}
