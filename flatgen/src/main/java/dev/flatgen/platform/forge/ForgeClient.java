package dev.flatgen.platform.forge;

import dev.flatgen.compat.blockpilot.BlockpilotSupport;
import dev.flatgen.library.client.DefaultPresets;
import dev.flatgen.options.FirstStarts;
import dev.flatgen.platform.ClientPlatform;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.fml.ModList;

/** Flatgen on a Forge client: the loader's answers, the game's events it listens to, and Blockpilot when it's there. */
final class ForgeClient {
    private ForgeClient() {
    }

    /** Installs the loader's answers and listens to the game's events. */
    static void start() {
        ClientPlatform.install(new ForgeClientPlatform());
        MinecraftForge.EVENT_BUS.addListener(ForgeClient::serverStarted);
        MinecraftForge.EVENT_BUS.addListener(ForgeClient::screenOpened);
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
