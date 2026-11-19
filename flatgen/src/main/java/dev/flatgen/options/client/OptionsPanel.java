package dev.flatgen.options.client;

import dev.flatgen.client.gui.Icon;
import dev.flatgen.client.gui.Label;
import dev.flatgen.client.gui.Toggle;
import dev.flatgen.client.gui.Widget;
import dev.flatgen.client.gui.rules.FlowLayout;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.editor.client.panel.EditorPanel;
import dev.flatgen.editor.client.panel.PanelSizes;
import dev.flatgen.options.rules.OptionChoices;
import dev.flatgen.options.rules.SetOptions;
import dev.flatgen.options.rules.WorldOption;
import dev.flatgen.platform.ClientPlatform;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.Component;

/**
 * The editor's world options: a picture switch for each option installed, under its group's heading, and a locked one
 * for each option the preset names that is no longer installed, kept as it is.
 */
public final class OptionsPanel implements EditorPanel {
    @Override
    public List<Widget> layOut(EditorContext editor, FlowLayout flow) {
        List<WorldOption> installed = editor.worldOptions();
        Map<String, List<WorldOption>> groups = new LinkedHashMap<>();
        installed.forEach(option -> groups.computeIfAbsent(option.group(), group -> new ArrayList<>()).add(option));
        List<Widget> widgets = new ArrayList<>();
        for (List<WorldOption> group : groups.values()) {
            widgets.add(new Label(icon(group.get(0)), groupTitle(group.get(0).group()), flow.line(Label.height())));
            group.forEach(option -> widgets.add(toggle(editor, option, installed, flow)));
            flow.newRow();
        }
        List<String> missing = OptionChoices.missing(editor.shown().preset().options(), installed);
        if (!missing.isEmpty()) {
            widgets.add(new Label(Icon.MISSING, Component.translatable("flatgen.options.missing"),
                    flow.line(Label.height())));
            missing.forEach(id -> widgets.add(missingToggle(editor, id, flow)));
        }
        return widgets;
    }

    private static Toggle toggle(EditorContext editor, WorldOption option, List<WorldOption> installed,
            FlowLayout flow) {
        return new Toggle(icon(option), Component.translatable(option.name()), flow.next(PanelSizes.DOUBLE_WIDTH),
                () -> OptionChoices.isOn(option, editor.shown().preset().options()),
                () -> turnOver(editor, option, installed), () -> true,
                () -> List.of(Component.translatable(option.description())));
    }

    /** Turns an option on or off, as one step to undo, turning off what clashes with it. */
    private static void turnOver(EditorContext editor, WorldOption option, List<WorldOption> installed) {
        Map<String, Boolean> chosen = editor.shown().preset().options();
        editor.apply(new SetOptions(OptionChoices.switched(chosen, option, !OptionChoices.isOn(option, chosen),
                installed)));
    }

    /** A switch for an option that is not installed: it shows how the preset keeps it, and cannot be turned over. */
    private static Toggle missingToggle(EditorContext editor, String id, FlowLayout flow) {
        return new Toggle(Icon.MISSING, Component.literal(id), flow.next(PanelSizes.DOUBLE_WIDTH),
                () -> editor.shown().preset().options().getOrDefault(id, false), () -> { }, () -> false,
                () -> List.of(Component.translatable("flatgen.options.not_installed", id)));
    }

    private static Icon icon(WorldOption option) {
        return option.icon().texture() ? Icon.texture(option.icon().id()) : Icon.item(option.icon().id());
    }

    /** A group's heading: a mod's name for a mod's id, or the translation of {@code option_group.<ns>.<path>}. */
    private static Component groupTitle(String group) {
        return group.contains(":") ? Component.translatable("option_group." + group.replace(':', '.'))
                : Component.literal(ClientPlatform.get().modName(group));
    }
}
