package dev.flatgen.options;

import dev.flatgen.mc.ServerFirstStart;
import dev.flatgen.options.rules.Effect;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.chunk.ChunkGenerator;

/**
 * The first-start effects of the world about to be created, run once when it first starts. They belong to the very
 * overworld generator Done put on the Create World screen, which no other world can have, so a world created after a
 * Cancel, reopened, or of another session never gets them (F27).
 */
public final class FirstStarts {
    private static final AtomicReference<Expected> EXPECTED = new AtomicReference<>();

    private FirstStarts() {
    }

    /**
     * Effects waiting for the world whose overworld has this generator.
     *
     * @param generator the overworld's generator, compared by identity
     * @param settings  its settings as JSON text, which a generator rebuilt alike has too
     * @param effects   the effects, in order
     */
    private record Expected(ChunkGenerator generator, String settings, List<Effect> effects) {
    }

    /**
     * Expects the world with this overworld generator, to run these effects on its first start; no effects forgets
     * any world expected.
     *
     * @param settings the generator's settings as JSON text, to follow it when the screen rebuilds it alike
     */
    public static void expect(ChunkGenerator generator, String settings, List<Effect> effects) {
        EXPECTED.set(effects.isEmpty() ? null : new Expected(generator, settings, List.copyOf(effects)));
    }

    /** Expects no world any more. */
    public static void forget() {
        EXPECTED.set(null);
    }

    /** Whether a world is expected whose overworld generator is not this one. */
    public static boolean expectsAnother(ChunkGenerator generator) {
        Expected expected = EXPECTED.get();
        return expected != null && expected.generator() != generator;
    }

    /**
     * The world on the Create World screen now has this overworld generator: when it is another with the settings
     * expected, as a data pack change rebuilds it, the world is still the one expected.
     */
    public static void follow(ChunkGenerator generator, String settings) {
        EXPECTED.updateAndGet(expected -> expected != null && !settings.isEmpty()
                && settings.equals(expected.settings()) ? new Expected(generator, settings, expected.effects())
                : expected);
    }

    /**
     * An integrated server has started, its overworld and spawn ready and no player in yet: runs the effects if its
     * world is the one expected, and forgets them either way, so they run once at most.
     */
    public static void serverStarted(MinecraftServer server) {
        Expected expected = EXPECTED.getAndSet(null);
        if (expected != null && server.overworld().getChunkSource().getGenerator() == expected.generator()) {
            ServerFirstStart start = new ServerFirstStart(server);
            expected.effects().forEach(effect -> effect.atFirstStart(start));
        }
    }
}
