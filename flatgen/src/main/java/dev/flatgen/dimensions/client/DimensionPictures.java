package dev.flatgen.dimensions.client;

import dev.flatgen.client.gui.Icon;
import dev.flatgen.dimensions.rules.DimensionColours;
import dev.flatgen.preset.rules.Dimension;
import net.minecraft.network.chat.Component;

/** What stands for each dimension in the editor (F22): its picture, its name and the colour of its air. */
public final class DimensionPictures {
    private DimensionPictures() {
    }

    /** A dimension's picture: a grass block, a lit Nether portal, end stone. */
    public static Icon of(Dimension dimension) {
        return switch (dimension) {
            case OVERWORLD -> Icon.block("minecraft:grass_block");
            case NETHER -> Icon.own("nether_portal");
            case END -> Icon.block("minecraft:end_stone");
        };
    }

    /**
     * The Nether or the End made flat, a neat stack of layers in its own blocks: the name of Flatgen's own picture of
     * it, the picture of making it flat.
     */
    public static String flat(Dimension dimension) {
        return dimension.key() + "_flat";
    }

    /**
     * The Nether or the End as the game makes it, its own rough ground (netherrack crags with lava, an End stone
     * island): the name of Flatgen's own picture of it, the picture of giving it back to the game.
     */
    public static String rough(Dimension dimension) {
        return dimension.key() + "_rough";
    }

    /** The colour of a dimension's air, drawn behind its layers and around the ground the game makes. */
    public static int air(Dimension dimension) {
        return switch (dimension) {
            case OVERWORLD -> DimensionColours.OVERWORLD_SKY;
            case NETHER -> DimensionColours.NETHER_SKY;
            case END -> DimensionColours.END_SKY;
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
