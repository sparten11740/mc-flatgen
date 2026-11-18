package dev.flatgen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** What every part of Flatgen names alike: its mod id and its log. */
public final class Flatgen {
    /** The mod id, the namespace of Flatgen's resources and the name of its config folder. */
    public static final String MOD_ID = "flatgen";
    /** Flatgen's log, for what goes wrong with files and settings. */
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private Flatgen() {
    }
}
