package dev.flatgen.platform.fabric;

import dev.flatgen.platform.ClientPlatform;
import net.fabricmc.loader.api.FabricLoader;

/** Fabric's answers to what Flatgen's shared code asks of the loader. */
final class FabricClientPlatform implements ClientPlatform {
    @Override
    public String modName(String namespace) {
        return FabricLoader.getInstance().getModContainer(namespace)
                .map(container -> container.getMetadata().getName()).orElse(namespace);
    }
}
