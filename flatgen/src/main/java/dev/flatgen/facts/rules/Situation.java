package dev.flatgen.facts.rules;

import dev.flatgen.biomes.rules.BiomeInfo;

/**
 * A world before it is made, as far as the facts need it.
 *
 * @param biome       its biome
 * @param mobSpawning whether mobs spawn on their own, as the world options leave the game rule
 * @param facts       what follows from the biome, the surface and mob spawning
 */
public record Situation(BiomeInfo biome, boolean mobSpawning, WorldFacts facts) {
    /** A world of this biome with its surface at this Y. */
    public static Situation of(BiomeInfo biome, int surfaceY, boolean mobSpawning) {
        return new Situation(biome, mobSpawning, WorldFacts.of(biome, surfaceY, mobSpawning));
    }
}
