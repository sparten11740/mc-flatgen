package dev.flatgen.client.gui.rules;

/**
 * Equal slots one below another, such as the layer cards: where each is, top first, and which one a height falls on.
 *
 * @param area  where the first slot starts and how wide the slots are; its height is not used
 * @param count how many slots there are
 * @param slot  each slot's height, in GUI pixels
 * @param gap   the space between slots, in GUI pixels
 */
public record ListSlots(Rect area, int count, int slot, int gap) {
    /** Where a slot is, by its place from the top. */
    public Rect at(int place) {
        return new Rect(area.x(), area.y() + place * (slot + gap), area.width(), slot);
    }

    /** The place of the slot at this height, the first or the last beyond the list's ends; -1 for no slots. */
    public int placeAt(double y) {
        if (count == 0) {
            return -1;
        }
        int place = (int) Math.floor((y - area.y()) / (slot + gap));
        return Math.max(0, Math.min(count - 1, place));
    }

    /** Where the last slot ends: the list's bottom edge. */
    public int bottom() {
        return area.y() + count * (slot + gap) - gap;
    }
}
