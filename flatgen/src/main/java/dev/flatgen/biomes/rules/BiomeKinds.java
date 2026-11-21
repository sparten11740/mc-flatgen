package dev.flatgen.biomes.rules;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * The kinds of biome the biome picker sorts by (docs/design/simple-editor.md, round 3): each a test on a biome's tags
 * and climate, so a mod's biomes sort in by the game's tags as the game's own do. A biome may be of several kinds or of
 * none; a new kind is one more entry in {@link #ALL}. How a biome is painted is {@link BiomeScenes}'.
 */
public final class BiomeKinds {
    /** Below this temperature what falls is snow rather than rain, where height does not cool a biome. */
    private static final float SNOW_BELOW = 0.15F;
    /** The tag of the biomes that make the Overworld. */
    private static final String OVERWORLD = "minecraft:is_overworld";
    /** The biomes most flat worlds are made in, one or two of each kind, shown first. */
    private static final List<String> USUAL = List.of("minecraft:plains", "minecraft:forest",
            "minecraft:birch_forest", "minecraft:dark_forest", "minecraft:meadow", "minecraft:cherry_grove",
            "minecraft:swamp", "minecraft:jungle", "minecraft:taiga", "minecraft:snowy_plains", "minecraft:desert",
            "minecraft:savanna", "minecraft:badlands", "minecraft:mushroom_fields", "minecraft:ocean",
            "minecraft:the_void");
    /** The tags mods put their swamps and caves in, as 1.21's common tags, 1.20.1 Fabric's and Forge's name them. */
    private static final List<String> MOD_SWAMPS = List.of("c:is_swamp", "c:swamp", "forge:is_swamp");
    private static final List<String> MOD_CAVES = List.of("c:is_cave", "c:is_underground", "c:caves", "c:underground",
            "forge:is_cave", "forge:is_underground");
    /** The game's cave biomes, which no tag of the game's own names. */
    private static final List<String> CAVES = List.of("minecraft:lush_caves", "minecraft:dripstone_caves",
            "minecraft:deep_dark");
    /** The tests of a biome's kinds that its picture shares ({@link BiomeScenes}): under the ground, snowy, dry, ... */
    static final Predicate<BiomeInfo> CAVE = tagged(MOD_CAVES).or(biome -> CAVES.contains(biome.id()));
    static final Predicate<BiomeInfo> SNOWY = biome -> overworld(biome) && biome.climate().precipitation()
            && biome.climate().temperature() < SNOW_BELOW;
    static final Predicate<BiomeInfo> DRY = biome -> overworld(biome) && !biome.climate().precipitation();
    static final Predicate<BiomeInfo> SWAMP = tagged(List.of("minecraft:allows_surface_slime_spawns"))
            .or(tagged(MOD_SWAMPS));
    static final Predicate<BiomeInfo> WATER = tagged("minecraft:is_ocean", "minecraft:is_deep_ocean",
            "minecraft:is_river", "minecraft:is_beach");
    static final Predicate<BiomeInfo> MOUNTAIN = tagged("minecraft:is_mountain", "minecraft:is_hill");
    static final Predicate<BiomeInfo> NETHER = tagged("minecraft:is_nether");
    static final Predicate<BiomeInfo> END = tagged("minecraft:is_end");
    static final Predicate<BiomeInfo> FOREST = tagged("minecraft:is_forest");
    static final Predicate<BiomeInfo> TAIGA = tagged("minecraft:is_taiga");
    static final Predicate<BiomeInfo> SAVANNA = tagged("minecraft:is_savanna");
    static final Predicate<BiomeInfo> JUNGLE = tagged("minecraft:is_jungle");
    static final Predicate<BiomeInfo> BEACH = tagged("minecraft:is_beach");

    /**
     * A kind of biome.
     *
     * @param name    its name's translation key
     * @param holds   whether a biome is of it
     * @param picture the biome whose landscape pictures it, by id, if the game has it; else its first biome's
     */
    public record Kind(String name, Predicate<BiomeInfo> holds, Optional<String> picture) {
    }

    /** Every kind, in the order the picker lists them. */
    public static final List<Kind> ALL = List.of(
            new Kind("flatgen.biomes.kind.usual", biome -> USUAL.contains(biome.id()),
                    Optional.of("minecraft:plains")),
            new Kind("flatgen.biomes.kind.forests",
                    FOREST.or(TAIGA).or(JUNGLE),
                    Optional.of("minecraft:forest")),
            new Kind("flatgen.biomes.kind.snowy", SNOWY, Optional.of("minecraft:snowy_plains")),
            new Kind("flatgen.biomes.kind.dry", DRY, Optional.of("minecraft:desert")),
            new Kind("flatgen.biomes.kind.swamps", SWAMP, Optional.of("minecraft:swamp")),
            new Kind("flatgen.biomes.kind.water", WATER, Optional.of("minecraft:ocean")),
            new Kind("flatgen.biomes.kind.mountains", MOUNTAIN, Optional.of("minecraft:jagged_peaks")),
            new Kind("flatgen.biomes.kind.caves", CAVE, Optional.of("minecraft:lush_caves")),
            new Kind("flatgen.biomes.kind.nether", NETHER,
                    Optional.of("minecraft:nether_wastes")),
            new Kind("flatgen.biomes.kind.end", END, Optional.of("minecraft:the_end")));

    private BiomeKinds() {
    }

    private static boolean overworld(BiomeInfo biome) {
        return biome.tags().contains(OVERWORLD);
    }

    /** The test of a biome being in any of these tags, named one by one. */
    static Predicate<BiomeInfo> tagged(String... tags) {
        return tagged(List.of(tags));
    }

    /** The test of a biome being in any of these tags. */
    private static Predicate<BiomeInfo> tagged(List<String> any) {
        return biome -> biome.tags().stream().anyMatch(any::contains);
    }
}
