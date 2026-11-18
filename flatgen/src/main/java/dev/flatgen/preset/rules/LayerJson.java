package dev.flatgen.preset.rules;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.flatgen.json.rules.JsonFields;
import java.util.ArrayList;
import java.util.List;

/**
 * Layers as JSON, bottom first, each {@code {"block": id, "height": blocks}}: the same in Flatgen's preset files and in
 * the game's own superflat settings.
 */
final class LayerJson {
    private static final String BLOCK = "block";
    private static final String HEIGHT = "height";

    private LayerJson() {
    }

    /** The layers of a list; a layer that cannot be read is left out and noted. */
    static List<Layer> read(JsonArray array, List<String> problems) {
        List<Layer> layers = new ArrayList<>();
        for (int index = 0; index < array.size(); index++) {
            JsonElement element = array.get(index);
            String where = "layer " + (index + 1);
            if (!element.isJsonObject() || !hasBlockAndHeight(element.getAsJsonObject())) {
                problems.add(where + " needs a block id and a height of at least 1, so it was left out");
                continue;
            }
            JsonObject layer = element.getAsJsonObject();
            layers.add(new Layer(layer.get(BLOCK).getAsString(), layer.get(HEIGHT).getAsInt()));
        }
        return layers;
    }

    /** The layers as a list. */
    static JsonArray write(List<Layer> layers) {
        JsonArray array = new JsonArray();
        for (Layer layer : layers) {
            JsonObject entry = new JsonObject();
            entry.addProperty(BLOCK, layer.block());
            entry.addProperty(HEIGHT, layer.height());
            array.add(entry);
        }
        return array;
    }

    private static boolean hasBlockAndHeight(JsonObject layer) {
        JsonElement block = layer.get(BLOCK);
        JsonElement height = layer.get(HEIGHT);
        return block != null && JsonFields.isText(block) && height != null && JsonFields.isInteger(height)
                && height.getAsInt() >= 1;
    }
}
