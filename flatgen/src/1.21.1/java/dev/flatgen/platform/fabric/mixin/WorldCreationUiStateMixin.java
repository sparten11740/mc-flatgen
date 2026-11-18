package dev.flatgen.platform.fabric.mixin;

import dev.flatgen.editor.client.PresetEditors;
import net.minecraft.client.gui.screens.worldselection.PresetEditor;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Fabric has no event for preset editors, so this hands the game Flatgen's editor for the world types it edits. */
@Mixin(WorldCreationUiState.class)
public abstract class WorldCreationUiStateMixin {
    @Shadow
    public abstract WorldCreationUiState.WorldTypeEntry getWorldType();

    @Inject(method = "getPresetEditor", at = @At("HEAD"), cancellable = true)
    private void flatgen$editFlatWorlds(CallbackInfoReturnable<PresetEditor> callback) {
        Holder<WorldPreset> preset = getWorldType().preset();
        if (preset != null && PresetEditors.edits(preset)) {
            callback.setReturnValue(PresetEditors.EDITOR);
        }
    }
}
