package dev.flatgen.mc;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** The game's items by id, as this version names and finds them. */
public final class Items {
    private Items() {
    }

    /** One of the item with this id, such as {@code minecraft:clock}; none for an id the game lacks. */
    public static ItemStack stack(String item) {
        ResourceLocation location = ResourceLocation.tryParse(item);
        return location == null ? ItemStack.EMPTY
                : BuiltInRegistries.ITEM.getOptional(location).map(ItemStack::new).orElse(ItemStack.EMPTY);
    }
}
