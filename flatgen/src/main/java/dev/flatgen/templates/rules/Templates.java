package dev.flatgen.templates.rules;

import dev.flatgen.editor.rules.World;
import dev.flatgen.preset.rules.Layer;
import dev.flatgen.preset.rules.Preset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Every template, in the order shown, and what they share; a new template is a class listed here. */
public final class Templates {
    /** Every template. */
    public static final List<Template> ALL = List.of(new TallFlat(), new AirshipField(), new WaterWorld(),
            new VoidWithPlatform(), new TestLayers());

    static final String BEDROCK = "minecraft:bedrock";
    static final String STONE = "minecraft:stone";
    static final String DIRT = "minecraft:dirt";
    static final String GRASS = "minecraft:grass_block";
    static final String PLAINS = "minecraft:plains";
    /** The surface Y templates start at: the lowest where slimes no longer spawn in slime chunks. */
    static final int NO_SLIMES_Y = 40;
    /** The surface Y a template's height setting may go from and to, kept within the world as it is made. */
    static final int LOWEST_SURFACE = -60;
    static final int HIGHEST_SURFACE = 300;
    static final Optional<List<String>> NO_STRUCTURES = Optional.of(List.of());

    private Templates() {
    }

    /** The stack's height for a surface at this Y in this world, at least the bedrock and the given top layers. */
    static int groundHeight(int surfaceY, World world, int topLayers) {
        return Math.max(topLayers + 1, Math.min(world.height(), surfaceY - world.floor()));
    }

    /** A template's preset, under its name, with no lakes. */
    static Preset preset(Template template, String icon, List<Layer> layers, String biome,
            Optional<List<String>> structureSets, boolean decorations, Map<String, Boolean> options) {
        return new Preset(template.name(), icon, layers, biome, structureSets, decorations, false, options);
    }
}
