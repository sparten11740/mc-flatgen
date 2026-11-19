package dev.flatgen.templates.rules;

import dev.flatgen.editor.rules.World;
import dev.flatgen.preset.rules.Layer;
import dev.flatgen.preset.rules.Preset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** A chosen number of thin layers of two blocks in turn, for testing machines and contraptions. */
final class TestLayers implements Template {
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
        List<Layer> layers = new ArrayList<>(List.of(new Layer(Templates.BEDROCK, 1)));
        for (int index = 0; index < values.number(COUNT); index++) {
            layers.add(new Layer(index % 2 == 0 ? values.block(FIRST) : values.block(SECOND), 1));
        }
        return Templates.preset(this, "minecraft:redstone", layers, Templates.PLAINS, Templates.NO_STRUCTURES, false,
                Map.of());
    }
}
