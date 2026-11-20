package dev.flatgen.platform.forge;

import dev.flatgen.Flatgen;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

/**
 * Flatgen's entry point on Forge. Forge constructs a mod on servers too, where Flatgen, a client mod, does nothing, so
 * the client's wiring is in a class of its own that a server never loads.
 */
@Mod(Flatgen.MOD_ID)
public final class ForgeEntrypoint {
    public ForgeEntrypoint() {
        if (FMLEnvironment.dist.isClient()) {
            ForgeClient.start();
        }
    }
}
