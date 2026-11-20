package dev.flatgen.options.client;

import dev.flatgen.client.gui.Icon;
import dev.flatgen.client.gui.Label;
import dev.flatgen.client.gui.Toggle;
import dev.flatgen.client.gui.Widget;
import dev.flatgen.client.gui.rules.FlowLayout;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.editor.client.panel.EditorLayout;
import dev.flatgen.editor.client.panel.EditorPanel;
import dev.flatgen.editor.client.panel.EditorRegion;
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
    /** Each group of options on a box of the aside, then the options not installed on a box of their own. */
    @Override
    public void layOut(EditorContext editor, EditorLayout layout) {
        List<WorldOption> installed = editor.worldOptions();
        Map<String, List<WorldOption>> groups = new LinkedHashMap<>();
        installed.forEach(option -> groups.computeIfAbsent(option.group(), group -> new ArrayList<>()).add(option));
        groups.values().forEach(group -> layout.box(inside -> group(editor, group, installed, inside)));
        List<String> missing = OptionChoices.missing(editor.shown().preset().options(), installed);
        if (!missing.isEmpty()) {
            layout.box(inside -> missing(editor, missing, inside));
        }
    }

    /**
     * A group's heading, with the picture the first of its options to give one gives the group, else its first option's
     * own; then a switch for each of its options.
     */
    private static List<Widget> group(EditorContext editor, List<WorldOption> group, List<WorldOption> installed,
            FlowLayout flow) {
        List<Widget> widgets = new ArrayList<>();
        Icon heading = group.stream().flatMap(option -> option.groupIcon().stream()).findFirst()
                .map(OptionsPanel::icon).orElse(icon(group.get(0).icon()));
        widgets.add(new Label(heading, groupTitle(group.get(0).group()), flow.line(Label.height())));
        group.forEach(option -> widgets.add(toggle(editor, option, installed, flow)));
        return widgets;
    }

    /** The heading of the options not installed, then a locked switch for each. */
    private static List<Widget> missing(EditorContext editor, List<String> missing, FlowLayout flow) {
        List<Widget> widgets = new ArrayList<>();
        widgets.add(new Label(Icon.MISSING, Component.translatable("flatgen.options.missing"),
                flow.line(Label.height())));
        missing.forEach(id -> widgets.add(missingToggle(editor, id, flow)));
        return widgets;
    }

    private static Toggle toggle(EditorContext editor, WorldOption option, List<WorldOption> installed,
            FlowLayout flow) {
        return new Toggle(icon(option.icon()), Component.translatable(option.name()),
                flow.line(EditorRegion.ASIDE.rowHeight()),
                () -> OptionChoices.isOn(option, editor.shown().preset().options()) != option.inverted(),
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
        return new Toggle(Icon.MISSING, Component.literal(id), flow.line(EditorRegion.ASIDE.rowHeight()),
                () -> editor.shown().preset().options().getOrDefault(id, false), () -> { }, () -> false,
                () -> List.of(Component.translatable("flatgen.options.not_installed", id)));
    }

    private static Icon icon(WorldOption.Icon icon) {
        return icon.texture() ? Icon.texture(icon.id()) : Icon.item(icon.id());
    }

    /** A group's heading: a mod's name for a mod's id, or the translation of {@code option_group.<ns>.<path>}. */
    private static Component groupTitle(String group) {
        return group.contains(":") ? Component.translatable("option_group." + group.replace(':', '.'))
                : Component.literal(ClientPlatform.get().modName(group));
    }
}
