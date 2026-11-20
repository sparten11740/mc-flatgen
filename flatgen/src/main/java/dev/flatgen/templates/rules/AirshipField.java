package dev.flatgen.templates.rules;

import dev.flatgen.editor.rules.World;
import dev.flatgen.preset.rules.Layer;
import dev.flatgen.preset.rules.Preset;
import java.util.List;
import java.util.Map;

/** An open field for airships at a chosen height: deep ground, grass on top, no structures, always clear. */
final class AirshipField implements Template {
    private static final Parameter.Number SURFACE = new Parameter.Number("surface_y", "flatgen.template.surface_y",
            "minecraft:ladder", Templates.LOWEST_SURFACE, Templates.HIGHEST_SURFACE, Templates.NO_SLIMES_Y);
    private static final Parameter.Toggle CLEAR = new Parameter.Toggle("always_clear",
            "flatgen.template.always_clear", "flatgen:textures/gui/sprites/icon/always_clear.png", true);
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
        int height = Templates.groundHeight(values.number(SURFACE), world, DIRT_DEPTH + 2);
        List<Layer> layers = List.of(new Layer(Templates.BEDROCK, 1),
                new Layer(Templates.STONE, height - DIRT_DEPTH - 2), new Layer(Templates.DIRT, DIRT_DEPTH),
                new Layer(Templates.GRASS, 1));
        return Templates.preset(this, Templates.GRASS, layers, Templates.PLAINS, Templates.NO_STRUCTURES, false,
                values.toggle(CLEAR) ? Map.of(ALWAYS_CLEAR, true) : Map.of());
    }
}
