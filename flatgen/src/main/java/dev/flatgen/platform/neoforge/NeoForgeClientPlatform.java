package dev.flatgen.platform.neoforge;

import dev.flatgen.platform.ClientPlatform;
import net.neoforged.fml.ModList;

/** NeoForge's answers to what Flatgen's shared code asks of the loader. */
final class NeoForgeClientPlatform implements ClientPlatform {
    @Override
    public String modName(String namespace) {
        return ModList.get().getModContainerById(namespace)
                .map(container -> container.getModInfo().getDisplayName()).orElse(namespace);
    }
}
