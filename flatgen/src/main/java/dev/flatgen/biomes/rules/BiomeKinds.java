package dev.flatgen.biomes.rules;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * The kinds of biome the biome picker sorts by (docs/design/simple-editor.md, round 3): each a test on a biome's tags
 * and climate, so a mod's biomes sort in by the game's tags as the game's own do, and most with the scene a biome of it
 * is painted as. A biome may be of several kinds, and every biome is of the last; a new kind is one more entry in
 * {@link #ALL}.
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
    /** The game's cave biomes, which no tag of the game's own names. */
    private static final List<String> CAVES = List.of("minecraft:lush_caves", "minecraft:dripstone_caves",
            "minecraft:deep_dark");
    /** The grounds and skies of the scenes, RGB: as the blocks and the dimensions look. */
    private static final int DIRT = 0x86603E;
    private static final int SNOW = 0xF2F6F8;
    private static final int SAND = 0xDBCF8E;
    private static final int STONE = 0x7F7F7F;
    private static final int CAVE_DARK = 0x2B2B30;
    private static final int NETHER_SKY = 0x3A1414;
    private static final int NETHERRACK = 0x6E2A2A;
    private static final int END_SKY = 0x140E1F;
    private static final int END_STONE = 0xDBD8A0;
    /** How much of a swamp's scene is water, in hundredths of its width. */
    private static final int SWAMP_WATER = 45;
    /** How much of a sea's scene is water: all of it. */
    private static final int ALL_WATER = 100;

    /**
     * How a biome of a kind is painted: in its own colours where these say so.
     *
     * @param sky    the sky, RGB, or {@link #OWN} for the biome's own
     * @param ground the ground, RGB
     * @param grass  whether the biome's grass tops the ground
     * @param tree   whether a tree of the biome's leaves stands on it
     * @param water  how much of it is the biome's water, from the right, in hundredths of its width
     */
    public record Scene(int sky, int ground, boolean grass, boolean tree, int water) {
        /** A colour that stands for the biome's own. */
        public static final int OWN = -1;
        /** A biome of no kind with a scene of its own: its sky over dirt topped with its grass. */
        public static final Scene PLAIN = new Scene(OWN, DIRT, true, false, 0);
    }

    /**
     * A kind of biome.
     *
     * @param name  its name's translation key
     * @param scene how a biome of it is painted; none for a kind that leaves that to the biome's others
     * @param holds whether a biome is of it
     */
    public record Kind(String name, Optional<Scene> scene, Predicate<BiomeInfo> holds) {
    }

    /** Every kind, in the order the picker lists them; the last, every biome. */
    public static final List<Kind> ALL = List.of(
            new Kind("flatgen.biomes.kind.usual", Optional.empty(),
                    biome -> USUAL.contains(biome.id())),
            new Kind("flatgen.biomes.kind.forests", scene(Scene.OWN, DIRT, true, true, 0),
                    tagged("minecraft:is_forest", "minecraft:is_taiga", "minecraft:is_jungle")),
            new Kind("flatgen.biomes.kind.snowy", scene(Scene.OWN, SNOW, false, false, 0),
                    biome -> overworld(biome) && biome.climate().precipitation()
                            && biome.climate().temperature() < SNOW_BELOW),
            new Kind("flatgen.biomes.kind.dry", scene(Scene.OWN, SAND, false, false, 0),
                    biome -> overworld(biome) && !biome.climate().precipitation()),
            new Kind("flatgen.biomes.kind.swamps", scene(Scene.OWN, DIRT, true, true, SWAMP_WATER),
                    tagged("minecraft:allows_surface_slime_spawns", "c:is_swamp")),
            new Kind("flatgen.biomes.kind.water", scene(Scene.OWN, SAND, false, false, ALL_WATER),
                    tagged("minecraft:is_ocean", "minecraft:is_deep_ocean", "minecraft:is_river",
                            "minecraft:is_beach")),
            new Kind("flatgen.biomes.kind.mountains", scene(Scene.OWN, STONE, true, false, 0),
                    tagged("minecraft:is_mountain", "minecraft:is_hill")),
            new Kind("flatgen.biomes.kind.caves", scene(CAVE_DARK, STONE, false, false, 0),
                    biome -> CAVES.contains(biome.id()) || tagged("c:is_cave", "c:is_underground").test(biome)),
            new Kind("flatgen.biomes.kind.nether", scene(NETHER_SKY, NETHERRACK, false, false, 0),
                    tagged("minecraft:is_nether")),
            new Kind("flatgen.biomes.kind.end", scene(END_SKY, END_STONE, false, false, 0),
                    tagged("minecraft:is_end")),
            new Kind("flatgen.biomes.kind.every", Optional.empty(),
                    biome -> true));

    private BiomeKinds() {
    }

    /** How a biome is painted: as the first of its kinds with a scene, else plain. */
    public static Scene scene(BiomeInfo biome) {
        return ALL.stream().filter(kind -> kind.scene().isPresent() && kind.holds().test(biome)).findFirst()
                .flatMap(Kind::scene).orElse(Scene.PLAIN);
    }

    private static Optional<Scene> scene(int sky, int ground, boolean grass, boolean tree, int water) {
        return Optional.of(new Scene(sky, ground, grass, tree, water));
    }

    private static boolean overworld(BiomeInfo biome) {
        return biome.tags().contains(OVERWORLD);
    }

    /** The test of a biome being in any of these tags. */
    private static Predicate<BiomeInfo> tagged(String... tags) {
        List<String> any = List.of(tags);
        return biome -> biome.tags().stream().anyMatch(any::contains);
    }
}
