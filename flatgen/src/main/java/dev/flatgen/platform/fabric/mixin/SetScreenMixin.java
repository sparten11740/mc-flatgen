package dev.flatgen.platform.fabric.mixin;

import dev.flatgen.library.client.DefaultPresets;
import dev.flatgen.mc.Screens;
//? if >=26.2 {
/*import net.minecraft.client.gui.Gui;
*///?} else {
import net.minecraft.client.Minecraft;
//?}
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fabric, without Fabric API, has no event for a screen that opened, so this tells Flatgen once a screen is set and
 * laid out: where NeoForge's {@code ScreenEvent.Init.Post} is, for the default preset to watch Create World. The game
 * sets its screen up to 26.1, its GUI from 26.2 on.
 */
//? if >=26.2 {
/*@Mixin(Gui.class)
*///?} else {
@Mixin(Minecraft.class)
//?}
public abstract class SetScreenMixin {
    @Inject(method = "setScreen", at = @At("RETURN"))
    private void flatgen$screenOpened(Screen requested, CallbackInfo callback) {
        Screen shown = Screens.current();
        if (shown != null) {
            DefaultPresets.screenOpened(shown);
        }
    }
}
