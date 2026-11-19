package dev.flatgen.client.gui;

import dev.flatgen.client.gui.rules.Rect;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;

/**
 * A fact as a picture: an icon on a dark tile (struck through, {@link Icon#struck}, for a "no"), with a tooltip saying
 * it in words. What it shows may change from frame to frame; when there is nothing to show it draws nothing.
 */
public final class Badge implements Control {
    /** The border around a badge's icon, in GUI pixels. */
    private static final int BORDER = 2;
    /** A badge's size, in GUI pixels: an icon and its border. */
    public static final int SIZE = Icon.SIZE + 2 * BORDER;
    private static final int TILE = 0x60000000;

    private final Rect bounds;
    private final Supplier<Optional<Look>> look;

    /**
     * What a badge shows.
     *
     * @param icon    its picture
     * @param tooltip what it says in words
     */
    public record Look(Icon icon, List<Component> tooltip) {
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
        now.get().icon().draw(canvas, bounds.x() + BORDER, bounds.y() + BORDER);
        if (bounds.contains(mouseX, mouseY)) {
            canvas.tooltip(now.get().tooltip(), mouseX, mouseY);
        }
    }

    @Override
    public List<Component> tooltip() {
        return look.get().map(Look::tooltip).orElse(List.of());
    }

    /** Its words, as its tooltip shows them. */
    @Override
    public Optional<Narration> narration() {
        return look.get().map(now -> Narration.said(now.tooltip()));
    }

    /** A badge takes the focus only while it shows a fact, so the ring never goes round nothing. */
    @Override
    public List<Widget> focusables() {
        return look.get().isPresent() ? List.of(this) : List.of();
    }

    @Override
    public Rect bounds() {
        return bounds;
    }
}
