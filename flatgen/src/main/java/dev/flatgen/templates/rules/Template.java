package dev.flatgen.templates.rules;

import dev.flatgen.editor.rules.World;
import dev.flatgen.preset.rules.Preset;
import java.util.List;

/**
 * A world described by a few settings rather than layers (F14), as "ground at Y 40": its settings, and the preset they
 * make. Each template is a class listed in {@link Templates}; a new one changes no other.
 */
public interface Template {
    /** Its name, a translation key. */
    String name();

    /** The id of the item its card shows. */
    String icon();

    /** Its settings, in the order they are shown. */
    List<Parameter> parameters();

    /** The preset these values make, kept within the world. */
    Preset make(Values values, World world);
}
