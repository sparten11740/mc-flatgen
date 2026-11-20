package dev.flatgen.platform.forge;

import dev.flatgen.platform.ClientPlatform;
import net.minecraftforge.fml.ModList;

/** Forge's answers to what Flatgen's shared code asks of the loader. */
final class ForgeClientPlatform implements ClientPlatform {
    @Override
    public String modName(String namespace) {
        return ModList.get().getModContainerById(namespace)
                .map(container -> container.getModInfo().getDisplayName()).orElse(namespace);
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }
}
