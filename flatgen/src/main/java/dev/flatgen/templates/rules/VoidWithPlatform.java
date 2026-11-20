package dev.flatgen.templates.rules;

import dev.flatgen.editor.rules.World;
import dev.flatgen.preset.rules.Layer;
import dev.flatgen.preset.rules.Preset;
import java.util.List;
import java.util.Map;

/** The void, with the game's stone platform at the spawn. */
final class VoidWithPlatform implements Template {
    @Override
    public String name() {
        return "flatgen.template.void_with_platform";
    }

    @Override
    public String about() {
        return "flatgen.template.void_with_platform.about";
    }

    @Override
    public String icon() {
        return "minecraft:stone";
    }

    @Override
    public List<Parameter> parameters() {
        return List.of();
    }

    @Override
    public Preset make(Values values, World world) {
        return Templates.preset(this, "minecraft:barrier", List.of(new Layer("minecraft:air", 1)), "minecraft:the_void",
                Templates.NO_STRUCTURES, true, Map.of());
    }
}
