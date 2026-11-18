package dev.flatgen.platform.neoforge;

import dev.flatgen.Flatgen;
import dev.flatgen.compat.blockpilot.BlockpilotCommands;
import dev.flatgen.editor.client.PresetEditors;
import dev.flatgen.options.FirstStarts;
import dev.flatgen.platform.ClientPlatform;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterPresetEditorsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/** Flatgen's entry point on NeoForge. Flatgen is a client mod, so NeoForge constructs it on the client only. */
@Mod(value = Flatgen.MOD_ID, dist = Dist.CLIENT)
public final class NeoForgeEntrypoint {
    public NeoForgeEntrypoint(IEventBus modBus) {
        ClientPlatform.install(new NeoForgeClientPlatform());
        modBus.addListener(NeoForgeEntrypoint::registerEditors);
        NeoForge.EVENT_BUS.addListener(NeoForgeEntrypoint::serverStarted);
        if (ModList.get().isLoaded(BlockpilotCommands.MOD_ID)) {
            BlockpilotCommands.register();
        }
    }

    /** Runs the first-start effects of the world just created, if the integrated server started is that world's. */
    private static void serverStarted(ServerStartedEvent event) {
        FirstStarts.serverStarted(event.getServer());
    }

    /** Hands NeoForge Flatgen's editor for the world types it edits, in place of the game's own. */
    private static void registerEditors(RegisterPresetEditorsEvent event) {
        PresetEditors.EDITED.forEach(key -> event.register(key, PresetEditors.EDITOR));
    }
}
