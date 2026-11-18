package dev.flatgen.options.rules;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.flatgen.json.rules.JsonFields;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * {@code place_at_spawn}: places a structure template once, on the world's first start, standing on the top layer at
 * the spawn point, and may move the spawn into it.
 *
 * @param template   the template's id, such as {@code skyliners:airport}
 * @param centred    whether the template's footprint is centred on the spawn's column, or else its origin is there
 * @param offset     how far it is moved after that, in blocks; a y of -1 sinks its floor into the top layer
 * @param rotation   how it is turned about its origin
 * @param voidHeight the Y its floor stands at in a world with no layers
 * @param spawn      the block of the template, before turning, the world's spawn moves to; none to leave it
 */
public record PlaceAtSpawn(String template, boolean centred, Position offset, Rotation rotation, int voidHeight,
        Optional<Position> spawn) implements Effect {
    /** The Y a template stands at in a world with no layers when the effect does not say, as a void world's spawn. */
    private static final int VOID_HEIGHT = 64;

    /**
     * Where the template goes, worked out on the first start.
     *
     * @param origin where the template's origin block goes, in the world
     * @param spawn  where the world's spawn moves to; none to leave it
     */
    public record Placement(Position origin, Optional<Position> spawn) {
    }

    /**
     * Reads {@code {"template": "skyliners:airport", "align": "center", "offset": [0, -1, 0], "rotation": "none",
     * "void_height": 64, "spawn": [20, 1, 8]}}; all but the template may be left out.
     */
    static Optional<Effect> read(JsonObject effect, List<String> problems) {
        String template = JsonFields.text(effect, "template", "", problems);
        String align = JsonFields.text(effect, "align", "center", problems);
        Optional<Rotation> rotation = Rotation.named(JsonFields.text(effect, "rotation", "none", problems));
        Optional<Position> offset = position(effect, "offset", problems);
        Optional<Position> spawn = position(effect, "spawn", problems);
        boolean readable = !template.isEmpty() && (align.equals("center") || align.equals("corner"))
                && rotation.isPresent() && (offset.isPresent() || !effect.has("offset"))
                && (spawn.isPresent() || !effect.has("spawn"));
        if (!readable) {
            problems.add("a place_at_spawn needs a template id, and an align of center or corner, a rotation the game"
                    + " names and an offset and spawn of three whole numbers where given");
            return Optional.empty();
        }
        return Optional.of(new PlaceAtSpawn(template, align.equals("center"), offset.orElse(new Position(0, 0, 0)),
                rotation.get(), JsonFields.integer(effect, "void_height", VOID_HEIGHT, problems), spawn));
    }

    @Override
    public void atFirstStart(FirstStart start) {
        start.placeAtSpawn(this);
    }

    @Override
    public boolean actsAtFirstStart() {
        return true;
    }

    /**
     * Where the template goes for a world's spawn column.
     *
     * @param size    the template's size in blocks, before turning
     * @param spawnX  the spawn's X
     * @param spawnZ  the spawn's Z
     * @param surface the first Y above the top layer at the spawn; none in a world with no layers
     */
    public Placement place(Position size, int spawnX, int spawnZ, OptionalInt surface) {
        Position corner = rotation.turn(new Position(size.x() - 1, 0, size.z() - 1));
        int left = Math.min(0, corner.x());
        int front = Math.min(0, corner.z());
        int alignX = centred ? -(left + Math.abs(corner.x()) / 2) : 0;
        int alignZ = centred ? -(front + Math.abs(corner.z()) / 2) : 0;
        Position base = new Position(spawnX + alignX, surface.orElse(voidHeight), spawnZ + alignZ);
        Position origin = base.plus(offset);
        return new Placement(origin, spawn.map(block -> origin.plus(rotation.turn(block))));
    }

    /** The key's three whole numbers as a position; none when it is missing or not three whole numbers. */
    private static Optional<Position> position(JsonObject effect, String key, List<String> problems) {
        Optional<JsonArray> numbers = JsonFields.array(effect, key, problems);
        if (numbers.isEmpty() || numbers.get().size() != 3) {
            return Optional.empty();
        }
        for (JsonElement number : numbers.get()) {
            if (!JsonFields.isInteger(number)) {
                return Optional.empty();
            }
        }
        JsonArray xyz = numbers.get();
        return Optional.of(new Position(xyz.get(0).getAsInt(), xyz.get(1).getAsInt(), xyz.get(2).getAsInt()));
    }
}
