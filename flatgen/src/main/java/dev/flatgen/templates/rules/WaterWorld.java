package dev.flatgen.templates.rules;

import dev.flatgen.editor.rules.World;
import dev.flatgen.preset.rules.Layer;
import dev.flatgen.preset.rules.Preset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Water of a chosen depth over a floor of a chosen block and depth, in an ocean. */
final class WaterWorld implements Template {
    private static final Parameter.Number WATER = new Parameter.Number("water_depth",
            "flatgen.template.water_depth", "minecraft:water_bucket", 1, 200, 30);
    private static final Parameter.Block FLOOR = new Parameter.Block("floor", "flatgen.template.floor",
            "minecraft:sand");
    private static final Parameter.Number FLOOR_DEPTH = new Parameter.Number("floor_depth",
            "flatgen.template.floor_depth", "minecraft:iron_shovel", 1, 50, 5);

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
        int floor = Math.min(values.number(FLOOR_DEPTH), world.height() - 2);
        int water = Math.min(values.number(WATER), world.height() - 1 - floor);
        return Templates.preset(this, "minecraft:water_bucket", List.of(new Layer(Templates.BEDROCK, 1),
                new Layer(values.block(FLOOR), floor), new Layer("minecraft:water", water)), "minecraft:ocean",
                Optional.empty(), true, Map.of());
    }
}
