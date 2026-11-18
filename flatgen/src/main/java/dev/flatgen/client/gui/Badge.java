package dev.flatgen.client.gui;

import dev.flatgen.client.gui.rules.Rect;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;

/**
 * A fact as a picture: an icon on a dark tile, struck through in red when the fact is a "no", with a tooltip saying it
 * in words. What it shows may change from frame to frame; when there is nothing to show it draws nothing.
 */
public final class Badge implements Widget {
    /** A badge's size, in GUI pixels: an icon and a two pixel border. */
    public static final int SIZE = Icon.SIZE + 4;
    private static final int TILE = 0x60000000;
    private static final int STRIKE = 0xFFE03030;
    /** The thickness of the red stroke, in GUI pixels. */
    private static final int STROKE = 2;

    private final Rect bounds;
    private final Supplier<Optional<Look>> look;

    /**
     * What a badge shows.
     *
     * @param icon    its picture
     * @param no      whether the picture is struck through, saying "no"
     * @param tooltip what it says in words
     */
    public record Look(Icon icon, boolean no, List<Component> tooltip) {
    }

    public Badge(Rect bounds, Supplier<Optional<Look>> look) {
        this.bounds = bounds;
        this.look = look;
    }

    @Override
    public void draw(Canvas canvas, int mouseX, int mouseY) {
        Optional<Look> now = look.get();
        if (now.isEmpty()) {
            return;
        }
        canvas.fill(bounds, TILE);
        now.get().icon().draw(canvas, bounds.x() + 2, bounds.y() + 2);
        if (now.get().no()) {
            strike(canvas);
        }
        if (bounds.contains(mouseX, mouseY)) {
            canvas.tooltip(now.get().tooltip(), mouseX, mouseY);
        }
    }

    /** A red stroke from the top left corner to the bottom right. */
    private void strike(Canvas canvas) {
        for (int step = 0; step < bounds.width() - STROKE; step++) {
            canvas.fill(new Rect(bounds.x() + step, bounds.y() + step, STROKE, STROKE), STRIKE);
        }
    }
}
