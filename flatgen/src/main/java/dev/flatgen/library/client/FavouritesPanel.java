package dev.flatgen.library.client;

import dev.flatgen.client.gui.Icon;
import dev.flatgen.client.gui.PictureButton;
import dev.flatgen.client.gui.Widget;
import dev.flatgen.client.gui.rules.FlowLayout;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.editor.client.panel.EditorPanel;
import dev.flatgen.library.ChoicesFile;
import dev.flatgen.library.PresetStore;
import dev.flatgen.preset.rules.Preset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;

/**
 * The editor's favourite presets (F12): a row with a picture of each, its top block, which uses it in one click; the
 * library is where they are starred.
 */
public final class FavouritesPanel implements EditorPanel {
    @Override
    public List<Widget> layOut(EditorContext editor, FlowLayout flow) {
        PresetStore store = PresetStore.player();
        List<Widget> widgets = new ArrayList<>();
        flow.newRow();
        for (String file : ChoicesFile.read().picks().starred()) {
            Optional<Preset> preset = store.read(file);
            preset.ifPresent(found -> widgets.add(new PictureButton(Icon.block(found.icon()),
                    flow.next(PictureButton.SIZE), () -> use(editor, file, found),
                    () -> List.of(Component.literal(found.name()),
                            Component.translatable("flatgen.library.use_tip")))));
        }
        return widgets;
    }

    private static void use(EditorContext editor, String file, Preset preset) {
        ChoicesFile.save(ChoicesFile.read().used(file));
        PresetUse.use(editor, editor.screen(), preset);
    }
}
