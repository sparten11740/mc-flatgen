package dev.flatgen.options.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import java.util.OptionalInt;
import org.junit.jupiter.api.Test;

/** Where a template placed at spawn goes. */
class PlaceAtSpawnTest {
    private static final Position FIVE_BY_THREE = new Position(5, 2, 3);

    @Test
    void centresTheFootprintOnTheSpawnAndStandsOnTheTopLayer() {
        PlaceAtSpawn.Placement placement = placement(true, Rotation.NONE, new Position(0, 0, 0))
                .place(FIVE_BY_THREE, 100, 200, OptionalInt.of(-60));
        assertEquals(new Position(98, -60, 199), placement.origin());
    }

    @Test
    void centresATurnedTemplateToo() {
        PlaceAtSpawn.Placement placement = placement(true, Rotation.CLOCKWISE_90, new Position(0, 0, 0))
                .place(FIVE_BY_THREE, 100, 200, OptionalInt.of(-60));
        assertEquals(new Position(101, -60, 198), placement.origin());
        assertEquals(new Position(99, -60, 198), placement.origin().plus(Rotation.CLOCKWISE_90.turn(
                new Position(0, 0, 2))));
    }

    @Test
    void putsTheOriginOnTheSpawnMovedByTheOffsetAndMovesTheSpawnIntoIt() {
        PlaceAtSpawn effect = new PlaceAtSpawn("a:b", false, new Position(1, -1, 0), Rotation.CLOCKWISE_180, 64,
                Optional.of(new Position(2, 1, 1)));
        PlaceAtSpawn.Placement placement = effect.place(FIVE_BY_THREE, 0, 0, OptionalInt.of(4));
        assertEquals(new Position(1, 3, 0), placement.origin());
        assertEquals(Optional.of(new Position(-1, 4, -1)), placement.spawn());
    }

    @Test
    void standsAtTheVoidHeightInAWorldWithNoLayers() {
        assertEquals(64, placement(false, Rotation.NONE, new Position(0, 0, 0))
                .place(FIVE_BY_THREE, 0, 0, OptionalInt.empty()).origin().y());
    }

    private static PlaceAtSpawn placement(boolean centred, Rotation rotation, Position offset) {
        return new PlaceAtSpawn("a:b", centred, offset, rotation, 64, Optional.empty());
    }
}
