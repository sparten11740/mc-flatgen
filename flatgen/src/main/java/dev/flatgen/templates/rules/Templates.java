package dev.flatgen.templates.rules;

import dev.flatgen.editor.rules.World;
import dev.flatgen.preset.rules.Layer;
import dev.flatgen.preset.rules.Preset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Every template, in the order shown; a new template is a class here. */
public final class Templates {
    /** Every template. */
    public static final List<Template> ALL = List.of(new TallFlat(), new AirshipField(), new WaterWorld(),
            new VoidWithPlatform(), new TestLayers());

    private static final String BEDROCK = "minecraft:bedrock";
    private static final String STONE = "minecraft:stone";
    private static final String DIRT = "minecraft:dirt";
    private static final String GRASS = "minecraft:grass_block";
    private static final String PLAINS = "minecraft:plains";
    /** The surface Y templates start at: the lowest where slimes no longer spawn in slime chunks. */
    private static final int NO_SLIMES_Y = 40;
    /** The surface Y a template's height setting may go from and to, kept within the world as it is made. */
    private static final int LOWEST_SURFACE = -60;
    private static final int HIGHEST_SURFACE = 300;
    private static final Optional<List<String>> NO_STRUCTURES = Optional.of(List.of());

    private Templates() {
    }

    /** Ground at a chosen Y (F14): bedrock, a filler block up to just below the surface, and a top block. */
    static final class TallFlat implements Template {
        private static final Parameter.Number SURFACE = new Parameter.Number("surface_y",
                "flatgen.template.surface_y", LOWEST_SURFACE, HIGHEST_SURFACE, NO_SLIMES_Y);
        private static final Parameter.Block TOP = new Parameter.Block("top", "flatgen.template.top", GRASS);
        private static final Parameter.Block FILLER = new Parameter.Block("filler", "flatgen.template.filler", STONE);

        @Override
        public String name() {
            return "flatgen.template.tall_flat";
        }

        @Override
        public String icon() {
            return GRASS;
        }

        @Override
        public List<Parameter> parameters() {
            return List.of(SURFACE, TOP, FILLER);
        }

        @Override
        public Preset make(Values values, World world) {
            int height = groundHeight(values.number(SURFACE), world, 2);
            return preset(this, values.block(TOP), List.of(new Layer(BEDROCK, 1),
                    new Layer(values.block(FILLER), height - 2), new Layer(values.block(TOP), 1)), PLAINS,
                    Optional.empty(), false, Map.of());
        }
    }

    /** An open field for airships at a chosen height: deep ground, grass on top, no structures, always clear. */
    static final class AirshipField implements Template {
        private static final Parameter.Number SURFACE = new Parameter.Number("surface_y",
                "flatgen.template.surface_y", LOWEST_SURFACE, HIGHEST_SURFACE, NO_SLIMES_Y);
        private static final Parameter.Toggle CLEAR = new Parameter.Toggle("always_clear",
                "flatgen.template.always_clear", "minecraft:sunflower", true);
        /** The world option that keeps the weather clear (F10). */
        private static final String ALWAYS_CLEAR = "flatgen:always_clear";
        private static final int DIRT_DEPTH = 3;

        @Override
        public String name() {
            return "flatgen.template.airship_field";
        }

        @Override
        public String icon() {
            return "minecraft:elytra";
        }

        @Override
        public List<Parameter> parameters() {
            return List.of(SURFACE, CLEAR);
        }

        @Override
        public Preset make(Values values, World world) {
            int height = groundHeight(values.number(SURFACE), world, DIRT_DEPTH + 2);
            return preset(this, GRASS, List.of(new Layer(BEDROCK, 1), new Layer(STONE, height - DIRT_DEPTH - 2),
                    new Layer(DIRT, DIRT_DEPTH), new Layer(GRASS, 1)), PLAINS, NO_STRUCTURES, false,
                    values.toggle(CLEAR) ? Map.of(ALWAYS_CLEAR, true) : Map.of());
        }
    }

    /** Water of a chosen depth over a floor of a chosen block and depth, in an ocean. */
    static final class WaterWorld implements Template {
        private static final Parameter.Number WATER = new Parameter.Number("water_depth",
                "flatgen.template.water_depth", 1, 200, 30);
        private static final Parameter.Block FLOOR = new Parameter.Block("floor", "flatgen.template.floor",
                "minecraft:sand");
        private static final Parameter.Number FLOOR_DEPTH = new Parameter.Number("floor_depth",
                "flatgen.template.floor_depth", 1, 50, 5);

        @Override
        public String name() {
            return "flatgen.template.water_world";
        }

        @Override
        public String icon() {
            return "minecraft:water_bucket";
        }

        @Override
        public List<Parameter> parameters() {
            return List.of(WATER, FLOOR, FLOOR_DEPTH);
        }

        @Override
        public Preset make(Values values, World world) {
            return preset(this, "minecraft:water_bucket", List.of(new Layer(BEDROCK, 1),
                    new Layer(values.block(FLOOR), values.number(FLOOR_DEPTH)),
                    new Layer("minecraft:water", values.number(WATER))), "minecraft:ocean", Optional.empty(), true,
                    Map.of());
        }
    }

    /** The void, with the game's stone platform at the spawn. */
    static final class VoidWithPlatform implements Template {
        @Override
        public String name() {
            return "flatgen.template.void_with_platform";
        }

        @Override
        public String icon() {
            return "minecraft:barrier";
        }

        @Override
        public List<Parameter> parameters() {
            return List.of();
        }

        @Override
        public Preset make(Values values, World world) {
            return preset(this, "minecraft:barrier", List.of(new Layer("minecraft:air", 1)), "minecraft:the_void",
                    NO_STRUCTURES, true, Map.of());
        }
    }

    /** A chosen number of thin layers of two blocks in turn, for testing machines and contraptions. */
    static final class TestLayers implements Template {
        private static final Parameter.Number COUNT = new Parameter.Number("count", "flatgen.template.count", 2, 64,
                8);
        private static final Parameter.Block FIRST = new Parameter.Block("first", "flatgen.template.first",
                "minecraft:white_concrete");
        private static final Parameter.Block SECOND = new Parameter.Block("second", "flatgen.template.second",
                "minecraft:black_concrete");

        @Override
        public String name() {
            return "flatgen.template.test_layers";
        }

        @Override
        public String icon() {
            return "minecraft:redstone";
        }

        @Override
        public List<Parameter> parameters() {
            return List.of(COUNT, FIRST, SECOND);
        }

        @Override
        public Preset make(Values values, World world) {
            List<Layer> layers = new ArrayList<>(List.of(new Layer(BEDROCK, 1)));
            for (int index = 0; index < values.number(COUNT); index++) {
                layers.add(new Layer(index % 2 == 0 ? values.block(FIRST) : values.block(SECOND), 1));
            }
            return preset(this, "minecraft:redstone", layers, PLAINS, NO_STRUCTURES, false, Map.of());
        }
    }

    /** The stack's height for a surface at this Y in this world, at least the bedrock and the given top layers. */
    private static int groundHeight(int surfaceY, World world, int topLayers) {
        return Math.max(topLayers + 1, Math.min(world.height(), surfaceY - world.floor()));
    }

    private static Preset preset(Template template, String icon, List<Layer> layers, String biome,
            Optional<List<String>> structureSets, boolean decorations, Map<String, Boolean> options) {
        return new Preset(template.name(), icon, layers, biome, structureSets, decorations, false, options);
    }
}
