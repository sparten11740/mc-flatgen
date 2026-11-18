package dev.flatgen.preset.rules;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Presets the unit tests share. */
public final class TestPresets {
    /** The stack most of the test instance's superflat worlds use: bedrock 1, stone 100, dirt 2, grass 1. */
    public static final Preset TALL_FLAT = new Preset("Tall flat", "minecraft:grass_block", List.of(
            new Layer("minecraft:bedrock", 1),
            new Layer("minecraft:stone", 100),
            new Layer("minecraft:dirt", 2),
            new Layer("minecraft:grass_block", 1)),
            "minecraft:plains", Optional.of(List.of("minecraft:strongholds", "minecraft:villages")), false, false,
            Map.of("flatgen:always_day", true));

    private TestPresets() {
    }
}
