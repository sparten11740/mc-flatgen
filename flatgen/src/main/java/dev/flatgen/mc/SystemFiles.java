package dev.flatgen.mc;

import dev.flatgen.Flatgen;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
//? if >=26.3 {
/*import com.mojang.blaze3d.Blaze3D;
*///?} else {
import net.minecraft.Util;
//?}

/** The player's own files, as the game opens them in the system's file browser. */
public final class SystemFiles {
    private SystemFiles() {
    }

    /** Opens a folder in the system's file browser, making it first when it does not exist yet. */
    public static void openFolder(Path folder) {
        try {
            Files.createDirectories(folder);
        } catch (IOException exception) {
            Flatgen.LOGGER.warn("Could not make {}", folder, exception);
        }
        //? if >=26.3 {
        /*Blaze3D.openPath(folder);
        *///?} else {
        Util.getPlatform().openPath(folder);
        //?}
    }
}
