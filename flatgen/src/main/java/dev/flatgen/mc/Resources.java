package dev.flatgen.mc;

import dev.flatgen.Flatgen;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

/** The client's resources, from the game, mods and resource packs, as this version lists and reads them. */
public final class Resources {
    private static final String JSON = ".json";

    private Resources() {
    }

    /**
     * Every JSON file in a folder of {@code assets/<namespace>/}, by {@code <namespace>:<name>}, its path within the
     * folder without {@code .json}; the topmost pack's file wins, as the game's own resources do. A file that cannot be
     * read is logged and left out.
     */
    public static Map<String, String> jsonFiles(String folder) {
        Map<String, String> files = new TreeMap<>();
        Map<ResourceLocation, Resource> found = Minecraft.getInstance().getResourceManager()
                .listResources(folder, location -> location.getPath().endsWith(JSON));
        found.forEach((location, resource) -> read(location, resource).ifPresent(text -> files.put(
                location.getNamespace() + ":" + location.getPath().substring(folder.length() + 1,
                        location.getPath().length() - JSON.length()), text)));
        return files;
    }

    private static Optional<String> read(ResourceLocation location, Resource resource) {
        try (BufferedReader reader = resource.openAsReader()) {
            return Optional.of(reader.lines().collect(Collectors.joining("\n")));
        } catch (IOException exception) {
            Flatgen.LOGGER.warn("Flatgen could not read {} from {}: {}", location, resource.sourcePackId(),
                    exception.getMessage());
            return Optional.empty();
        }
    }
}
