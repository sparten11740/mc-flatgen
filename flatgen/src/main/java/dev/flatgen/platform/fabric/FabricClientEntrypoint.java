package dev.flatgen.platform.fabric;

import dev.flatgen.compat.blockpilot.BlockpilotCommands;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

/** Flatgen's entry point on Fabric, the client entry point fabric.mod.json names; the editor itself is a mixin. */
public final class FabricClientEntrypoint implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        if (FabricLoader.getInstance().isModLoaded(BlockpilotCommands.MOD_ID)) {
            BlockpilotCommands.register();
        }
    }
}
