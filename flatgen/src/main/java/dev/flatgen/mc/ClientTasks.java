package dev.flatgen.mc;

import net.minecraft.client.Minecraft;

/** Work for the game's client thread, as this version queues it. */
public final class ClientTasks {
    private ClientTasks() {
    }

    /**
     * Runs a task on the client thread once what it is doing now is done, as after a listener has been told of a change
     * it must not answer with changes of its own.
     */
    public static void later(Runnable task) {
        Minecraft.getInstance().tell(task);
    }
}
