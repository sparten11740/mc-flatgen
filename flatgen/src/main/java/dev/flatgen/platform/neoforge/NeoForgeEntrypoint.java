package dev.flatgen.platform.neoforge;

import dev.flatgen.Flatgen;
import dev.flatgen.compat.blockpilot.BlockpilotSupport;
import dev.flatgen.library.client.DefaultPresets;
import dev.flatgen.options.FirstStarts;
import dev.flatgen.platform.ClientPlatform;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/** Flatgen's entry point on NeoForge. Flatgen is a client mod, so NeoForge constructs it on the client only. */
@Mod(value = Flatgen.MOD_ID, dist = Dist.CLIENT)
public final class NeoForgeEntrypoint {
    public NeoForgeEntrypoint() {
        ClientPlatform.install(new NeoForgeClientPlatform());
        NeoForge.EVENT_BUS.addListener(NeoForgeEntrypoint::serverStarted);
        NeoForge.EVENT_BUS.addListener(NeoForgeEntrypoint::screenOpened);
        ModList.get().getModContainerById(BlockpilotSupport.MOD_ID)
                .ifPresent(blockpilot -> BlockpilotSupport.installed(blockpilot.getModInfo().getVersion().toString()));
    }

    /** Runs the first-start effects of the world just created, if the integrated server started is that world's. */
    private static void serverStarted(ServerStartedEvent event) {
        FirstStarts.serverStarted(event.getServer());
    }

    /** Lets the default preset watch a Create World screen that opened. */
    private static void screenOpened(ScreenEvent.Init.Post event) {
        DefaultPresets.screenOpened(event.getScreen());
    }
}
