package dev.flatgen.platform.neoforge;

import dev.flatgen.Flatgen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

/** Flatgen's entry point on NeoForge. Flatgen is a client mod, so NeoForge constructs it on the client only. */
@Mod(value = Flatgen.MOD_ID, dist = Dist.CLIENT)
public final class NeoForgeEntrypoint {
}
