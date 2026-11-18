package dev.flatgen.mc;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

/** The names players read for the ids a preset holds, in the game's language; the id itself when the game lacks it. */
public final class Names {
    private Names() {
    }

    /** A block's name. */
    public static Component block(String id) {
        ResourceLocation location = ResourceLocation.tryParse(id);
        return location == null ? Component.literal(id)
                : BuiltInRegistries.BLOCK.getOptional(location).map(Block::getName).orElse(Component.literal(id));
    }

    /** A biome's name, from its translation key; the game shows the key itself for a biome it has no name for. */
    public static Component biome(String id) {
        ResourceLocation location = ResourceLocation.tryParse(id);
        return location == null ? Component.literal(id) : Component.translatable(location.toLanguageKey("biome"));
    }
}
