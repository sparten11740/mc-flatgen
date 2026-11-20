package dev.flatgen.biomes.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * How a biome is painted as a little landscape, so a player who cannot read tells one from another: its sky, its
 * ground and the shapes standing on it (a tree of its kind, a peak behind, a mushroom, flowers), each picked by the
 * biome's tags, name and climate, so a mod's biomes are painted as the game's of their kind. A new look is one more
 * rule in a list; the painting only draws what a scene holds.
 */
public final class BiomeScenes {
    /** A colour standing for the biome's own sky, grass or leaves; clear, so that no colour painted is it. */
    public static final int OWN = 0x00000001;
    /** No colour, clear too: a ground with nothing on top. */
    public static final int NONE = 0;
    /** How much of the picture is ground, in hundredths of its height. */
    public static final int GROUND_SHARE = 30;
    /** The colours of the scenes' parts, ARGB, as the blocks and the dimensions look. */
    private static final int DIRT = 0xFF86603E;
    private static final int SAND = 0xFFDBCF8E;
    private static final int SNOW = 0xFFF2F6F8;
    private static final int STONE = 0xFF7F7F7F;
    private static final int STONE_LIGHT = 0xFF9A9A9A;
    private static final int NETHERRACK = 0xFF6E2A2A;
    private static final int END_STONE = 0xFFDBD8A0;
    private static final int MYCELIUM = 0xFF8E7496;
    private static final int TRUNK = 0xFF6B4A2C;
    private static final int BIRCH_TRUNK = 0xFFE4E0D2;
    private static final int BIRCH_MARK = 0xFF3A3A3A;
    private static final int CHERRY_LEAVES = 0xFFF2A9C9;
    private static final int MUSHROOM_RED = 0xFFC62A2A;
    private static final int MUSHROOM_STEM = 0xFFE8DCC8;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int POPPY = 0xFFD03030;
    private static final int DANDELION = 0xFFF0D020;
    private static final int CORNFLOWER = 0xFF4A6AE0;
    private static final List<Integer> TERRACOTTA = List.of(0xFFB05A2A, 0xFFD0884A, 0xFF9A3E2A, 0xFFC8A060,
            0xFFB86A3A);
    private static final int VOID_SKY = 0xFF14161C;
    private static final int CAVE_DARK = 0xFF2B2B30;
    private static final int NETHER_SKY = 0xFF3A1414;
    private static final int END_SKY = 0xFF140E1F;
    /** How much of a sea's scene is water, a beach's and a swamp's, in hundredths of its width. */
    private static final int ALL_WATER = 100;
    private static final int BEACH_WATER = 40;
    private static final int SWAMP_WATER = 45;

    /**
     * A ground: its colour, what tops it, and bands of colour across it instead, as the badlands' terracotta.
     *
     * @param body  its colour, ARGB, or {@link BiomeScenes#NONE} for no ground at all
     * @param top   the colour of its top, ARGB, {@link BiomeScenes#OWN} for the biome's grass or
     *              {@link BiomeScenes#NONE} for none
     * @param bands colours across it from its top down, each a band; none for one colour
     */
    public record Ground(int body, int top, List<Integer> bands) {
        public Ground {
            bands = List.copyOf(bands);
        }

    }

    /**
     * A rectangle standing on the ground: from the picture's left and from the ground's top up, in hundredths of the
     * picture's width and height, and its colour.
     *
     * @param x      its left, in hundredths of the width
     * @param bottom its bottom above the ground's top, in hundredths of the height
     * @param width  its width, in hundredths of the width
     * @param height its height, in hundredths of the height
     * @param colour ARGB, or {@link BiomeScenes#OWN} for the biome's leaves
     */
    public record Patch(int x, int bottom, int width, int height, int colour) {
    }

    /**
     * A biome's landscape.
     *
     * @param sky     the sky, ARGB, or {@link BiomeScenes#OWN} for the biome's own, under which the sun shines
     * @param ground  the ground
     * @param patches what stands on the ground, drawn in order
     * @param water   how much of the ground is the biome's water, from the right, in hundredths of the width
     */
    public record Scene(int sky, Ground ground, List<Patch> patches, int water) {
        public Scene {
            patches = List.copyOf(patches);
        }
    }

    private static final Ground GRASS = new Ground(DIRT, OWN, List.of());
    private static final Ground SAND_ONLY = new Ground(SAND, NONE, List.of());
    private static final Ground BADLANDS = new Ground(TERRACOTTA.get(0), NONE, TERRACOTTA);
    private static final Ground MYCELIUM_TOP = new Ground(DIRT, MYCELIUM, List.of());
    private static final Ground SNOW_ONLY = new Ground(SNOW, NONE, List.of());
    private static final Ground STONE_GRASS = new Ground(STONE, OWN, List.of());
    private static final Ground STONE_ONLY = new Ground(STONE, NONE, List.of());
    private static final Ground NETHER = new Ground(NETHERRACK, NONE, List.of());
    private static final Ground END = new Ground(END_STONE, NONE, List.of());
    private static final Ground NOTHING = new Ground(NONE, NONE, List.of());

    /** A look a biome may have: the test it passes, and the look. */
    private record Rule<T>(Predicate<BiomeInfo> holds, T look) {
    }

    private static final List<Patch> OAK = List.of(new Patch(22, 0, 6, 18, TRUNK), new Patch(12, 16, 26, 28, OWN));
    private static final List<Patch> BIRCH = List.of(new Patch(22, 0, 6, 22, BIRCH_TRUNK),
            new Patch(22, 8, 6, 3, BIRCH_MARK), new Patch(14, 20, 22, 24, OWN));
    private static final List<Patch> SPRUCE = List.of(new Patch(23, 0, 4, 10, TRUNK), new Patch(12, 8, 26, 12, OWN),
            new Patch(15, 18, 20, 12, OWN), new Patch(19, 28, 12, 12, OWN));
    private static final List<Patch> ACACIA = List.of(new Patch(24, 0, 5, 24, TRUNK), new Patch(6, 22, 40, 13, OWN));
    private static final List<Patch> CHERRY = List.of(new Patch(22, 0, 6, 16, TRUNK),
            new Patch(10, 14, 30, 28, CHERRY_LEAVES));
    private static final List<Patch> JUNGLE = List.of(new Patch(22, 0, 6, 36, TRUNK), new Patch(10, 32, 30, 24, OWN));
    private static final List<Patch> PEAK = List.of(new Patch(42, 0, 50, 14, STONE_LIGHT),
            new Patch(50, 14, 34, 12, STONE_LIGHT), new Patch(58, 26, 18, 10, STONE_LIGHT),
            new Patch(62, 32, 10, 5, SNOW));
    private static final List<Patch> MUSHROOM = List.of(new Patch(57, 0, 8, 16, MUSHROOM_STEM),
            new Patch(46, 14, 30, 18, MUSHROOM_RED), new Patch(51, 24, 6, 5, WHITE), new Patch(65, 19, 6, 5, WHITE));
    private static final List<Patch> FLOWERS = List.of(new Patch(38, 0, 5, 9, POPPY),
            new Patch(52, 0, 5, 8, DANDELION), new Patch(66, 0, 5, 9, CORNFLOWER), new Patch(80, 0, 5, 8, POPPY));

    /** The sky: the dimension's or the dark of a cave or the void, else the biome's own. */
    private static final List<Rule<Integer>> SKIES = List.of(
            new Rule<>(named("void"), VOID_SKY),
            new Rule<>(BiomeKinds.CAVE, CAVE_DARK),
            new Rule<>(BiomeKinds.tagged("minecraft:is_nether"), NETHER_SKY),
            new Rule<>(BiomeKinds.tagged("minecraft:is_end"), END_SKY));
    /** The ground, the first that holds; grass topped dirt for the rest. */
    private static final List<Rule<Ground>> GROUNDS = List.of(
            new Rule<>(named("void"), NOTHING),
            new Rule<>(BiomeKinds.tagged("minecraft:is_badlands"), BADLANDS),
            new Rule<>(named("mushroom"), MYCELIUM_TOP),
            new Rule<>(BiomeKinds.tagged("minecraft:is_nether"), NETHER),
            new Rule<>(BiomeKinds.tagged("minecraft:is_end"), END),
            new Rule<>(BiomeKinds.CAVE, STONE_ONLY),
            new Rule<>(BiomeKinds.SNOWY, SNOW_ONLY),
            new Rule<>(BiomeKinds.WATER, SAND_ONLY),
            new Rule<>(named("meadow"), GRASS),
            new Rule<>(BiomeKinds.MOUNTAIN, STONE_GRASS),
            new Rule<>(BiomeKinds.tagged("minecraft:is_savanna"), GRASS),
            new Rule<>(BiomeKinds.DRY, SAND_ONLY));
    /** The tree standing on the ground, the first that holds; none for the rest. */
    private static final List<Rule<List<Patch>>> TREES = List.of(
            new Rule<>(named("cherry"), CHERRY),
            new Rule<>(named("birch"), BIRCH),
            new Rule<>(BiomeKinds.tagged("minecraft:is_taiga"), SPRUCE),
            new Rule<>(BiomeKinds.tagged("minecraft:is_savanna"), ACACIA),
            new Rule<>(BiomeKinds.tagged("minecraft:is_jungle"), JUNGLE),
            new Rule<>(BiomeKinds.tagged("minecraft:is_forest").or(BiomeKinds.SWAMP), OAK));
    /** More a biome has, each that holds: a peak behind, a mushroom, flowers. */
    private static final List<Rule<List<Patch>>> EXTRAS = List.of(
            new Rule<>(BiomeKinds.MOUNTAIN.and(named("meadow").negate()), PEAK),
            new Rule<>(named("mushroom"), MUSHROOM),
            new Rule<>(named("meadow").or(named("flower")), FLOWERS));
    /** How much of the ground is water, the first that holds; none for the rest. */
    private static final List<Rule<Integer>> WATERS = List.of(
            new Rule<>(BiomeKinds.tagged("minecraft:is_beach"), BEACH_WATER),
            new Rule<>(BiomeKinds.WATER, ALL_WATER),
            new Rule<>(BiomeKinds.SWAMP, SWAMP_WATER));

    private BiomeScenes() {
    }

    /** How a biome is painted. */
    public static Scene of(BiomeInfo biome) {
        List<Patch> patches = new ArrayList<>();
        EXTRAS.stream().filter(rule -> rule.holds().test(biome)).forEach(rule -> patches.addAll(rule.look()));
        first(TREES, biome).ifPresent(patches::addAll);
        return new Scene(first(SKIES, biome).orElse(OWN), first(GROUNDS, biome).orElse(GRASS), patches,
                first(WATERS, biome).orElse(0));
    }

    private static <T> Optional<T> first(List<Rule<T>> rules, BiomeInfo biome) {
        return rules.stream().filter(rule -> rule.holds().test(biome)).findFirst().map(Rule::look);
    }

    /** The test of a biome's name, the path of its id, having this word in it, as {@code birch_forest} has birch. */
    private static Predicate<BiomeInfo> named(String word) {
        return biome -> biome.id().substring(biome.id().indexOf(':') + 1).contains(word);
    }
}
