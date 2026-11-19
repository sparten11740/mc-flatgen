package dev.flatgen.dimensions.client;

import dev.flatgen.client.gui.Icon;
import dev.flatgen.preset.rules.Dimension;
import net.minecraft.network.chat.Component;

/** What stands for each dimension in the editor (F22): its picture and its name. */
public final class DimensionPictures {
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

    /** A dimension's name, as the game's players call it. */
    public static Component name(Dimension dimension) {
        return Component.translatable(switch (dimension) {
            case OVERWORLD -> "flatgen.dimension.overworld";
            case NETHER -> "flatgen.dimension.nether";
            case END -> "flatgen.dimension.end";
        });
    }
}
