package dev.flatgen.editor.client.mixin;

import dev.flatgen.editor.client.PresetEditors;
import java.util.Optional;
import net.minecraft.client.gui.screens.worldselection.PresetEditor;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Hands the Create World screen Flatgen's editor for the world types {@link PresetEditors} says it edits, after the
 * game, or NeoForge's list of editors, has found its own, on both loaders alike: NeoForge's event registers editors by
 * world type before the world types a mod adds are known, so a type superflat underneath could not be told by it.
 */
@Mixin(WorldCreationUiState.class)
public abstract class WorldCreationUiStateMixin {
    @Shadow
    public abstract WorldCreationUiState.WorldTypeEntry getWorldType();

    @Shadow
    public abstract WorldCreationContext getSettings();

    @Inject(method = "getPresetEditor", at = @At("RETURN"), cancellable = true)
    private void flatgen$editFlatWorlds(CallbackInfoReturnable<PresetEditor> callback) {
        Holder<WorldPreset> preset = getWorldType().preset();
        if (preset != null) {
            callback.setReturnValue(PresetEditors.editorFor(preset, Optional.ofNullable(callback.getReturnValue()),
                    getSettings().worldgenLoadContext()).orElse(null));
        }
    }
}
