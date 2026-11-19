package dev.flatgen.gametest;

import static dev.flatgen.gametest.FlatgenTests.check;

import dev.flatgen.facts.rules.WorldFacts;
import dev.flatgen.mc.Biomes;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
//? if >=26.2 {
/*import net.minecraft.world.entity.EntityTypes;
*///?} else {
import net.minecraft.world.entity.EntityType;
//?}
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.WorldgenRandom;

/** The world facts (F08) held to the game's own rules: its precipitation and its slime spawning. */
final class FactGameTests {
    /** The biomes F08 names, and one whose snow line a tall stack reaches. */
    private static final List<String> BIOMES = List.of("minecraft:plains", "minecraft:desert",
            "minecraft:snowy_plains", "minecraft:swamp", "minecraft:mushroom_fields", "minecraft:windswept_hills");
    /** The surface heights F08 names, and one above the windswept hills' snow line. */
    private static final List<Integer> HEIGHTS = List.of(-10, -2, 39, 40, 60, 100, 150);
    /** The salt the game mixes into a slime chunk's seed (1.21.1). */
    private static final long SLIME_SALT = 987234911L;

    private FactGameTests() {
    }

    static Map<String, Consumer<GameTestHelper>> tests() {
        Map<String, Consumer<GameTestHelper>> tests = new LinkedHashMap<>();
        tests.put("the weather facts agree with the game's precipitation", FactGameTests::weather);
        tests.put("the slime fact agrees with the game's slime chunks", FactGameTests::slimeChunks);
        return tests;
    }

    private static void weather(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (String id : BIOMES) {
            Biome biome = level.registryAccess().lookupOrThrow(Registries.BIOME)
                    .getOrThrow(ResourceKey.create(Registries.BIOME, ResourceLocation.parse(id))).value();
            HEIGHTS.forEach(y -> weatherAgrees(level, id, biome, y));
        }
    }

    private static void weatherAgrees(ServerLevel level, String id, Biome biome, int y) {
        WorldFacts facts = WorldFacts.of(Biomes.info(id, level.registryAccess()).orElseThrow(), y, true);
        //? if >=1.21.2 {
        /*String game = biome.getPrecipitationAt(new BlockPos(0, y, 0), level.getSeaLevel()).name();
        *///?} else {
        String game = biome.getPrecipitationAt(new BlockPos(0, y, 0)).name();
        //?}
        check(facts.weather().name().equals(game), id + " at Y " + y + ": " + facts.weather() + ", the game " + game);
    }

    /** On a slime chunk with a block to stand on, the game spawns slimes below Y 40 only, as the fact says. */
    private static void slimeChunks(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        check(level.getDifficulty() != Difficulty.PEACEFUL, "the GameTest world is peaceful, where no slime spawns");
        ChunkPos chunk = slimeChunk(level.getSeed());
        for (int y : List.of(WorldFacts.SLIME_CHUNK_MAX_Y - 1, WorldFacts.SLIME_CHUNK_MAX_Y)) {
            BlockPos pos = new BlockPos(chunk.getMiddleBlockX(), y, chunk.getMiddleBlockZ());
            level.setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
            boolean game = Slime.checkSlimeSpawnRules(EntityType.SLIME, level, MobSpawnType.NATURAL, pos,
                    luckyRandom());
            boolean fact = WorldFacts.of(Biomes.info("minecraft:plains", level.registryAccess()).orElseThrow(), y,
                    true).slimeChunks();
            check(game == fact, "at Y " + y + " the game says " + game + ", the fact " + fact);
        }
    }

    /** The first chunk along the X axis that is a slime chunk in a world of this seed. */
    private static ChunkPos slimeChunk(long seed) {
        int x = 0;
        while (WorldgenRandom.seedSlimeChunk(x, 0, seed, SLIME_SALT).nextInt(10) != 0) {
            x++;
        }
        return new ChunkPos(x, 0);
    }

    /** A random source whose first roll of ten is 0, the one in ten chance a slime chunk spawns a slime. */
    private static RandomSource luckyRandom() {
        long seed = 0;
        while (RandomSource.create(seed).nextInt(10) != 0) {
            seed++;
        }
        return RandomSource.create(seed);
    }
}
