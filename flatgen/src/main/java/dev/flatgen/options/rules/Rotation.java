package dev.flatgen.options.rules;

import java.util.Locale;
import java.util.Optional;

/** How a template is turned, seen from above, about its origin, by the names the game gives them. */
public enum Rotation {
    NONE("none"),
    CLOCKWISE_90("clockwise_90"),
    CLOCKWISE_180("180"),
    COUNTERCLOCKWISE_90("counterclockwise_90");

    private final String gameName;

    Rotation(String gameName) {
        this.gameName = gameName;
    }

    /** The rotation of this name, as the game writes it. */
    public static Optional<Rotation> named(String name) {
        for (Rotation rotation : values()) {
            if (rotation.gameName.equals(name.toLowerCase(Locale.ROOT))) {
                return Optional.of(rotation);
            }
        }
        return Optional.empty();
    }

    /** Where a block of the template lands, from its origin, once turned, as the game turns templates. */
    public Position turn(Position block) {
        switch (this) {
            case CLOCKWISE_90:
                return new Position(-block.z(), block.y(), block.x());
            case CLOCKWISE_180:
                return new Position(-block.x(), block.y(), -block.z());
            case COUNTERCLOCKWISE_90:
                return new Position(block.z(), block.y(), -block.x());
            default:
                return block;
        }
    }
}
