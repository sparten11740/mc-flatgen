package dev.flatgen.platform.fabric.mixin;

import dev.flatgen.library.client.DefaultPresets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fabric, without Fabric API, has no event for a screen that opened, so this tells Flatgen once a screen is set and
 * laid out: where NeoForge's {@code ScreenEvent.Init.Post} is, for the default preset to watch Create World.
 */
@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow
    public Screen screen;

    @Inject(method = "setScreen", at = @At("RETURN"))
    private void flatgen$screenOpened(Screen requested, CallbackInfo callback) {
        if (screen != null) {
            DefaultPresets.screenOpened(screen);
        }
    }
}
