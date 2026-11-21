package dev.flatgen.pictures.rules;

/**
 * A picture Flatgen's rules and files name: an item, by its id, or a texture 16 by 16 pixels, by its location, as world
 * options' files write them ({@code "minecraft:clock"} or {@code {"texture": "mymod:textures/gui/airport.png"}}).
 *
 * @param id      an item's id, or a texture's location
 * @param texture whether it is a texture rather than an item
 */
public record Picture(String id, boolean texture) {
    /** An item's picture, by its id, such as {@code minecraft:clock}. */
    public static Picture item(String id) {
        return new Picture(id, false);
    }

    /** A texture 16 by 16 pixels, by its location, such as {@code flatgen:textures/gui/sprites/icon/rain.png}. */
    public static Picture texture(String location) {
        return new Picture(location, true);
    }
}
