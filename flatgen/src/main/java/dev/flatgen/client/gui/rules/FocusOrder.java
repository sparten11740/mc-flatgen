package dev.flatgen.client.gui.rules;

import java.util.List;
import java.util.Optional;

/**
 * Where the keyboard's focus moves among a screen's areas and the controls in each: Tab to the next area that has
 * controls, going round, and the arrows from control to control within an area. An area is told by how many controls
 * it has, so a screen that lays itself out again keeps its focus where its controls are.
 */
public final class FocusOrder {
    private FocusOrder() {
    }

    /**
     * A control's place.
     *
     * @param area    its area, by its index among the screen's
     * @param control its index among its area's controls
     */
    public record Place(int area, int control) {
    }

    /**
     * Where Tab moves the focus: to the first control of the next area with controls, or the previous one going back,
     * going round; from nowhere, to the first such area, or the last going back. None when no area has controls.
     *
     * @param controls how many controls each area has
     */
    public static Optional<Place> tab(List<Integer> controls, Optional<Place> from, boolean forward) {
        int areas = controls.size();
        int start = from.map(Place::area).orElse(forward ? -1 : areas);
        for (int step = 1; step <= areas; step++) {
            int area = Math.floorMod(start + (forward ? step : -step), areas);
            if (controls.get(area) > 0) {
                return Optional.of(new Place(area, 0));
            }
        }
        return Optional.empty();
    }

    /**
     * Where an arrow moves the focus: to the next or the previous control of its area; none at the area's end, so the
     * arrow can do something else there.
     */
    public static Optional<Place> step(List<Integer> controls, Place from, boolean forward) {
        int control = from.control() + (forward ? 1 : -1);
        boolean inside = from.area() < controls.size() && control >= 0 && control < controls.get(from.area());
        return inside ? Optional.of(new Place(from.area(), control)) : Optional.empty();
    }

    /** A place kept within the areas as they are now; none when its area is gone or has no controls left. */
    public static Optional<Place> within(List<Integer> controls, Place place) {
        if (place.area() >= controls.size() || controls.get(place.area()) == 0) {
            return Optional.empty();
        }
        return Optional.of(new Place(place.area(), Math.min(place.control(), controls.get(place.area()) - 1)));
    }
}
