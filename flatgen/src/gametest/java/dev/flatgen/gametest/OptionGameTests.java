package dev.flatgen.gametest;

import static dev.flatgen.gametest.FlatgenTests.check;

import dev.flatgen.options.FirstStarts;
import dev.flatgen.options.rules.FirstStartTime;
import dev.flatgen.options.rules.PlaceAtSpawn;
import dev.flatgen.options.rules.Position;
import dev.flatgen.options.rules.Rotation;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;

/** World options' first-start effects (F27): run once, on the world expected only, as the fixture says. */
final class OptionGameTests {
    /** The fixture's template: a pad five blocks square with a lantern on each corner. */
    private static final String PAD = "flatgen_fixture:landing_pad";
    private static final Position PAD_SIZE = new Position(5, 2, 5);
    /** Where the spawn moves to in the pad, before turning. */
    private static final Position PAD_SPAWN = new Position(2, 1, 2);
    private static final PlaceAtSpawn PLACE_PAD = new PlaceAtSpawn(PAD, true, new Position(0, 0, 0), Rotation.NONE,
            64, Optional.of(PAD_SPAWN));
    /** The time the effects set, and one they never do. */
    private static final long NOON = 6000;
    private static final long MORNING = 1000;

    private OptionGameTests() {
    }

    static Map<String, Consumer<GameTestHelper>> tests() {
        Map<String, Consumer<GameTestHelper>> tests = new LinkedHashMap<>();
        tests.put("first-start effects run once on the world expected", OptionGameTests::runOnce);
        tests.put("first-start effects never run on another world", OptionGameTests::onlyTheWorldExpected);
        return tests;
    }

    private static void runOnce(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Restore restore = Restore.of(level);
        BlockPos site = helper.absolutePos(new BlockPos(0, 0, 0));
        level.setDefaultSpawnPos(site, 0);
        level.setDayTime(MORNING);
        PlaceAtSpawn.Placement placement = PLACE_PAD.place(PAD_SIZE, site.getX(), site.getZ(),
                OptionalInt.of(level.getHeight(Heightmap.Types.WORLD_SURFACE, site.getX(), site.getZ())));
        BlockPos origin = blockPos(placement.origin());
        FirstStarts.expect(generator(level), "", List.of(PLACE_PAD, new FirstStartTime(NOON)));
        FirstStarts.serverStarted(level.getServer());
        check(level.getBlockState(origin).is(Blocks.BLACK_CONCRETE), "the pad's corner is not at " + origin);
        check(level.getBlockState(origin.offset(2, 0, 2)).is(Blocks.YELLOW_CONCRETE), "the pad's middle is missing");
        check(level.getBlockState(origin.above()).is(Blocks.LANTERN), "the pad's lantern is missing");
        check(level.getSharedSpawnPos().equals(blockPos(placement.spawn().orElseThrow())),
                "the spawn is " + level.getSharedSpawnPos() + ", not on the pad");
        check(level.getDayTime() == NOON, "the time is " + level.getDayTime() + ", not noon");
        level.setBlockAndUpdate(origin, Blocks.AIR.defaultBlockState());
        level.setDayTime(MORNING);
        FirstStarts.serverStarted(level.getServer());
        check(level.getBlockState(origin).isAir() && level.getDayTime() == MORNING, "the effects ran twice");
        restore.run();
    }

    private static void onlyTheWorldExpected(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Restore restore = Restore.of(level);
        level.setDayTime(MORNING);
        RegistryAccess registries = level.registryAccess();
        ChunkGenerator another = new FlatLevelSource(FlatLevelGeneratorSettings.getDefault(
                registries.lookupOrThrow(Registries.BIOME), registries.lookupOrThrow(Registries.STRUCTURE_SET),
                registries.lookupOrThrow(Registries.PLACED_FEATURE)));
        FirstStarts.expect(another, "", List.of(new FirstStartTime(NOON)));
        FirstStarts.serverStarted(level.getServer());
        check(level.getDayTime() == MORNING, "the effects ran on a world with another generator");
        restore.run();
    }

    private static ChunkGenerator generator(ServerLevel level) {
        return level.getServer().overworld().getChunkSource().getGenerator();
    }

    private static BlockPos blockPos(Position position) {
        return new BlockPos(position.x(), position.y(), position.z());
    }

    /** The spawn and time a test found, to put back. */
    private record Restore(ServerLevel level, BlockPos spawn, float angle, long time) implements Runnable {
        static Restore of(ServerLevel level) {
            return new Restore(level, level.getSharedSpawnPos(), level.getSharedSpawnAngle(), level.getDayTime());
        }

        @Override
        public void run() {
            level.setDefaultSpawnPos(spawn, angle);
            level.setDayTime(time);
        }
    }
}
