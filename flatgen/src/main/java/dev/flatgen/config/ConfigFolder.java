package dev.flatgen.config;

import dev.flatgen.Flatgen;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;

/** config/flatgen in the game folder, where every file Flatgen keeps for the player lives. */
public final class ConfigFolder {
    private ConfigFolder() {
    }

    /** The folder, which may not exist yet. */
    public static Path path() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve(Flatgen.MOD_ID);
    }
}
