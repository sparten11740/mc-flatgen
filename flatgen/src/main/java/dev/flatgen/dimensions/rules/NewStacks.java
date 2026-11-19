package dev.flatgen.dimensions.rules;

import dev.flatgen.preset.rules.Dimension;
import dev.flatgen.preset.rules.Layer;
import dev.flatgen.preset.rules.Stack;
import java.util.List;

/** The stack the Nether or the End starts with when the player makes it flat (F22), standing where the game expects. */
public final class NewStacks {
    /** Netherrack above the Nether's bedrock, in blocks: the surface at Y 32, where the Nether's lava sea lies. */
    static final int NETHERRACK_DEPTH = 31;
    /**
     * End stone above the End's bedrock, in blocks: the top at Y 48, the layer the game lays its obsidian platform in
     * for a player arriving through a stronghold's portal, who then steps out onto the ground.
     */
    static final int END_STONE_DEPTH = 48;
    private static final Layer BEDROCK = new Layer("minecraft:bedrock", 1);

    private NewStacks() {
    }

    /**
     * The Nether's or the End's first stack: bedrock at its floor under its own ground, in its own biome.
     *
     * @throws IllegalArgumentException for the overworld, which always has its stack
     */
    public static Stack of(Dimension dimension) {
        List<Layer> layers = switch (dimension) {
            case OVERWORLD -> throw new IllegalArgumentException("the overworld always has a stack");
            case NETHER -> List.of(BEDROCK, new Layer("minecraft:netherrack", NETHERRACK_DEPTH));
            case END -> List.of(BEDROCK, new Layer("minecraft:end_stone", END_STONE_DEPTH));
        };
        return new Stack(layers, dimension.defaultBiome());
    }
}
