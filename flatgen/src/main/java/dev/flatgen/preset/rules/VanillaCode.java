package dev.flatgen.preset.rules;

import java.util.stream.Collectors;

/**
 * The game's superflat code, the text its presets screen shares worlds as:
 * {@code minecraft:bedrock,2*minecraft:dirt,minecraft:grass_block;minecraft:plains}, the layers bottom up, each
 * {@code [height*]block}, then the biome. Reading it comes with F06.
 */
public final class VanillaCode {
    private VanillaCode() {
    }

    /** The code of a preset's layers and biome, exactly as the game's presets screen writes it. */
    public static String write(Preset preset) {
        return preset.layers().stream().map(VanillaCode::layer).collect(Collectors.joining(",")) + ";" + preset.biome();
    }

    private static String layer(Layer layer) {
        return (layer.height() != 1 ? layer.height() + "*" : "") + layer.block();
    }
}
