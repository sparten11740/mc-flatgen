package dev.flatgen.platform.neoforge;

import dev.flatgen.Flatgen;
import dev.flatgen.editor.client.PresetEditors;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterPresetEditorsEvent;

/** Flatgen's entry point on NeoForge. Flatgen is a client mod, so NeoForge constructs it on the client only. */
@Mod(value = Flatgen.MOD_ID, dist = Dist.CLIENT)
public final class NeoForgeEntrypoint {
    public NeoForgeEntrypoint(IEventBus modBus) {
        modBus.addListener(NeoForgeEntrypoint::registerEditors);
    }

    /** Hands NeoForge Flatgen's editor for the world types it edits, in place of the game's own. */
    private static void registerEditors(RegisterPresetEditorsEvent event) {
        PresetEditors.EDITED.forEach(key -> event.register(key, PresetEditors.EDITOR));
    }
}
