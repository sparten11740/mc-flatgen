package dev.flatgen.platform.fabric;

import dev.flatgen.compat.blockpilot.BlockpilotSupport;
import dev.flatgen.platform.ClientPlatform;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

/** Flatgen's entry point on Fabric, the client entry point fabric.mod.json names; the editor itself is a mixin. */
public final class FabricClientEntrypoint implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlatform.install(new FabricClientPlatform());
        FabricLoader.getInstance().getModContainer(BlockpilotSupport.MOD_ID).ifPresent(blockpilot ->
                BlockpilotSupport.installed(blockpilot.getMetadata().getVersion().getFriendlyString()));
    }
}
