package dev.flatgen.platform.fabric;

import dev.flatgen.compat.blockpilot.BlockpilotCommands;
import dev.flatgen.platform.ClientPlatform;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

/** Flatgen's entry point on Fabric, the client entry point fabric.mod.json names; the editor itself is a mixin. */
public final class FabricClientEntrypoint implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlatform.install(new FabricClientPlatform());
        if (FabricLoader.getInstance().isModLoaded(BlockpilotCommands.MOD_ID)) {
            BlockpilotCommands.register();
        }
    }
}
