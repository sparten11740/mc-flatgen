package dev.flatgen.platform;

/**
 * What shared code asks of the loader it runs on, which each loader's platform half answers its own way. The entry
 * point installs its half before anything asks.
 */
public interface ClientPlatform {
    /** A mod's name for players, by the namespace of its ids; the namespace itself for one the loader does not know. */
    String modName(String namespace);

    /** Whether a mod of this id is loaded. */
    boolean isModLoaded(String modId);

    /** The loader's half, as installed. */
    static ClientPlatform get() {
        return Installed.platform;
    }

    /** Installs the loader's half; its entry point does this first. */
    static void install(ClientPlatform platform) {
        Installed.platform = platform;
    }

    /** Where the installed half is kept. */
    final class Installed {
        private static ClientPlatform platform;

        private Installed() {
        }
    }
}
