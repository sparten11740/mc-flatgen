package dev.flatgen.client.gui;

import dev.flatgen.client.gui.rules.Rect;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A number's step down and step up, with the number to type between them where there is one, as one box: the steps
 * drawn flat inside it and a line parting them where nothing is between, so that the three read as one control, as a
 * layer's thickness and a template's settings have it.
 */
public final class Stepper {
    /** How far the line parting the two steps stays from the box's top and bottom, in GUI pixels. */
    private static final int LINE_INSET = 4;

    private Stepper() {
    }

    /**
     * A stepper's widgets, in the order the keyboard reaches them: its box, the step down, the number if there is one,
     * and the step up, which the caller has placed side by side, the step down at the left.
     */
    public static List<Widget> of(PictureButton down, Optional<NumberField> number, PictureButton up) {
        Rect box = new Rect(down.bounds().x(), down.bounds().y(), up.bounds().right() - down.bounds().x(),
                down.bounds().height());
        List<Widget> widgets = new ArrayList<>();
        widgets.add((canvas, mouseX, mouseY) -> draw(canvas, box, down.bounds(), up.bounds()));
        widgets.add(down.flat());
        number.ifPresent(field -> widgets.add(field.flat()));
        widgets.add(up.flat());
        return widgets;
    }

    /** The box the steps share, with the line parting them when nothing is between. */
    private static void draw(Canvas canvas, Rect box, Rect down, Rect up) {
        canvas.sprite(Look.BUTTON, box);
        if (down.right() == up.x()) {
            canvas.fill(new Rect(up.x(), box.y() + LINE_INSET, 1, box.height() - 2 * LINE_INSET), Look.EDGE);
        }
    }
}
