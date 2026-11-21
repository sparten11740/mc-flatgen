package dev.flatgen.templates.rules;

import dev.flatgen.editor.rules.World;
import dev.flatgen.pictures.rules.Picture;
import dev.flatgen.preset.rules.Layer;
import dev.flatgen.preset.rules.Preset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Ground at a chosen Y (F14): bedrock, a filler block up to just below the surface, and a top block. */
final class TallFlat implements Template {
    private static final Parameter.Number SURFACE = new Parameter.Number("surface_y", "flatgen.template.surface_y",
            Picture.item("minecraft:ladder"), Templates.LOWEST_SURFACE, Templates.HIGHEST_SURFACE, Templates.NO_SLIMES_Y);
    private static final Parameter.Block TOP = new Parameter.Block("top", "flatgen.template.top", Templates.GRASS);
    private static final Parameter.Block FILLER = new Parameter.Block("filler", "flatgen.template.filler",
            Templates.STONE);
    /** The layers of the stack that are not filler: the bedrock below it and the top block above it. */
    private static final int NOT_FILLER = 2;

    @Override
    public String name() {
        return "flatgen.template.tall_flat";
    }

    @Override
    public String about() {
        return "flatgen.template.tall_flat.about";
    }

    @Override
    public String icon() {
        return Templates.GRASS;
    }

    @Override
    public List<Parameter> parameters() {
        return List.of(SURFACE, TOP, FILLER);
    }

    @Override
    public Preset make(Values values, World world) {
        int height = Templates.groundHeight(values.number(SURFACE), world, NOT_FILLER);
        return Templates.preset(this, values.block(TOP), List.of(new Layer(Templates.BEDROCK, 1),
                new Layer(values.block(FILLER), height - NOT_FILLER), new Layer(values.block(TOP), 1)),
                Templates.PLAINS, Optional.empty(), false, Map.of());
    }
}
