package dev.flatgen.dimensions.client;

import dev.flatgen.client.gui.Icon;
import dev.flatgen.preset.rules.Dimension;
import net.minecraft.network.chat.Component;

/** What stands for each dimension in the editor (F22): its picture, its name and the colour of its air. */
public final class DimensionPictures {
    /** The colour of each dimension's air: the overworld's sky, the Nether's haze, the End's dark. ARGB. */
    private static final int OVERWORLD_AIR = 0xFF9CC3F7;
    private static final int NETHER_AIR = 0xFF3A1414;
    private static final int END_AIR = 0xFF15111F;

    private DimensionPictures() {
    }

    /** A dimension's picture: a grass block, a lit Nether portal, an Eye of Ender. */
    public static Icon of(Dimension dimension) {
        return switch (dimension) {
            case OVERWORLD -> Icon.block("minecraft:grass_block");
            case NETHER -> Icon.own("nether_portal");
            case END -> Icon.item("minecraft:ender_eye");
        };
    }

    /** The colour of a dimension's air, drawn behind its layers and around the ground the game makes. */
    public static int air(Dimension dimension) {
        return switch (dimension) {
            case OVERWORLD -> OVERWORLD_AIR;
            case NETHER -> NETHER_AIR;
            case END -> END_AIR;
        };
    }

    /** A dimension's name, as the game's players call it. */
    public static Component name(Dimension dimension) {
        return Component.translatable(switch (dimension) {
            case OVERWORLD -> "flatgen.dimension.overworld";
            case NETHER -> "flatgen.dimension.nether";
            case END -> "flatgen.dimension.end";
        });
    }
}
