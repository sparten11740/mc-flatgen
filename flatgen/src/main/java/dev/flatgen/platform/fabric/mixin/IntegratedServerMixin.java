package dev.flatgen.platform.fabric.mixin;

import dev.flatgen.options.FirstStarts;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fabric, without Fabric API, has no event for a server that started, so this tells Flatgen when the integrated server
 * has its worlds loaded and its spawn ready, before any player joins: where NeoForge's {@code ServerStartedEvent} is.
 */
@Mixin(IntegratedServer.class)
public abstract class IntegratedServerMixin {
    @Inject(method = "initServer", at = @At("RETURN"))
    private void flatgen$runFirstStartEffects(CallbackInfoReturnable<Boolean> callback) {
        if (callback.getReturnValueZ()) {
            FirstStarts.serverStarted((MinecraftServer) (Object) this);
        }
    }
}
