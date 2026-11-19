package dev.flatgen.facts.rules;

import dev.flatgen.biomes.rules.BiomeInfo;
import java.util.OptionalInt;

/**
 * What a superflat world will be like at its surface, worked out before it is created by the rules of Minecraft 1.21.1
 * ({@code Biome.getPrecipitationAt}, {@code Slime.checkSlimeSpawnRules}); the GameTests hold them to the game's own.
 *
 * @param surfaceY      the Y mobs stand at: the world's floor plus the stack's height
 * @param weather       what falls at the surface when it does not shine
 * @param snowLine      the Y snow starts at, about, when the surface is near it; none otherwise
 * @param slimeChunks   whether slimes spawn in slime chunks, the surface being low enough for them
 * @param surfaceSlimes whether slimes spawn on the surface at night, as in a swamp at the right height
 * @param hostileMobs   whether monsters spawn
 */
public record WorldFacts(int surfaceY, Weather weather, OptionalInt snowLine, boolean slimeChunks,
        boolean surfaceSlimes, boolean hostileMobs) {
    /** Slimes spawn in slime chunks below this Y (1.21.1). */
    public static final int SLIME_CHUNK_MAX_Y = 40;
    /** Slimes spawn on the surface of a swamp above this Y and below {@link #SURFACE_SLIME_ABOVE_MAX} (1.21.1). */
    private static final int SURFACE_SLIME_ABOVE = 50;
    private static final int SURFACE_SLIME_ABOVE_MAX = 70;
    /** It snows where the temperature is below this, and rains elsewhere (1.21.1). */
    private static final float SNOW_BELOW = 0.15f;
    /** How much the temperature drops for each block above the climate's cooling start (0.05 per 40 blocks). */
    private static final float COOLING_PER_BLOCK = 0.05f / 40;
    /** How far the game's temperature noise moves the snow line either way, in blocks, at most. */
    private static final int SNOW_LINE_NOISE = 8;

    /** What falls when it does not shine. */
    public enum Weather {
        /** Nothing: the biome has no rain or snow, as a desert. */
        NONE,
        RAIN,
        SNOW
    }

    /** The facts of a biome with its surface at this Y, mob spawning on or off by the game rules. */
    public static WorldFacts of(BiomeInfo biome, int surfaceY, boolean mobSpawning) {
        BiomeInfo.Climate climate = biome.climate();
        OptionalInt line = snowLine(climate);
        boolean nearLine = line.isPresent() && Math.abs(surfaceY - line.getAsInt()) <= SNOW_LINE_NOISE;
        boolean slimes = mobSpawning && biome.spawns().slimes();
        return new WorldFacts(surfaceY, weather(climate, surfaceY), nearLine ? line : OptionalInt.empty(),
                slimes && surfaceY < SLIME_CHUNK_MAX_Y,
                slimes && biome.spawns().surfaceSlimes() && surfaceY > SURFACE_SLIME_ABOVE
                        && surfaceY < SURFACE_SLIME_ABOVE_MAX,
                mobSpawning && biome.spawns().monsters());
    }

    /** What falls at this Y, the temperature taken without the game's noise. */
    private static Weather weather(BiomeInfo.Climate climate, int y) {
        if (!climate.precipitation()) {
            return Weather.NONE;
        }
        float cooling = Math.max(0, y - climate.coolingStartY()) * COOLING_PER_BLOCK;
        return climate.temperature() - cooling < SNOW_BELOW ? Weather.SNOW : Weather.RAIN;
    }

    /** The Y the biome's rain turns to snow at, about; none when it never rains in it or snows at any height. */
    public static OptionalInt snowLine(BiomeInfo.Climate climate) {
        if (!climate.precipitation() || climate.temperature() < SNOW_BELOW) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(climate.coolingStartY() + (int) Math.ceil((climate.temperature() - SNOW_BELOW)
                / COOLING_PER_BLOCK));
    }
}
