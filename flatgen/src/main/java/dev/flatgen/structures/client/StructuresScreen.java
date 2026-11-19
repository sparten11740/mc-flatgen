package dev.flatgen.structures.client;

import dev.flatgen.client.gui.FlatgenScreen;
import dev.flatgen.client.gui.Icon;
import dev.flatgen.client.gui.Label;
import dev.flatgen.client.gui.Look;
import dev.flatgen.client.gui.ScrollGroup;
import dev.flatgen.client.gui.SegmentedChoice;
import dev.flatgen.client.gui.Surface;
import dev.flatgen.client.gui.TextButton;
import dev.flatgen.client.gui.TextStyle;
import dev.flatgen.client.gui.Toggle;
import dev.flatgen.client.gui.Widget;
import dev.flatgen.client.gui.rules.FlowLayout;
import dev.flatgen.client.gui.rules.PageFrame;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.editor.client.panel.PanelSizes;
import dev.flatgen.mc.Names;
import dev.flatgen.mc.Screens;
import dev.flatgen.platform.ClientPlatform;
import dev.flatgen.preset.rules.Preset;
import dev.flatgen.structures.rules.SetDecorations;
import dev.flatgen.structures.rules.SetStructures;
import dev.flatgen.structures.rules.StructureChoice;
import dev.flatgen.structures.rules.StructureSetInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/**
 * Structures and decorations (F09), opened from the editor: whether the world takes the structures its biome allows,
 * none or a chosen list; every structure set of the world being created as a switch, grouped by mod and struck through
 * where it cannot appear in the biome; and switches for the biome's decorations and the lakes. Every change is a step
 * to undo in the editor.
 */
public final class StructuresScreen extends FlatgenScreen {
    /** How many of the biomes a set needs its tooltip names. */
    private static final int BIOMES_NAMED = 3;
    private static final Map<StructureChoice.Mode, Icon> MODE_ICONS = Map.of(
            StructureChoice.Mode.BIOME, Icon.item("minecraft:grass_block"),
            StructureChoice.Mode.NONE, Icon.struck(Icon.item("minecraft:chest")),
            StructureChoice.Mode.CHOSEN, Icon.item("minecraft:chest"));

    private final EditorContext editor;
    private final List<StructureSetInfo> sets;
    private final List<String> every;
    private final Set<String> fromOptions;

    /**
     * The screen for what the editor holds, over these structure sets.
     *
     * @param fromOptions the sets world options that are on add, shown on and locked
     */
    public StructuresScreen(EditorContext editor, List<StructureSetInfo> sets, Set<String> fromOptions) {
        super(Component.translatable("flatgen.structures.title"));
        this.editor = editor;
        this.sets = List.copyOf(sets);
        this.every = sets.stream().map(StructureSetInfo::id).toList();
        this.fromOptions = Set.copyOf(fromOptions);
    }

    @Override
    protected List<Widget> layOut(int width, int height) {
        PageFrame frame = new PageFrame(width, height);
        Rect body = frame.body();
        return List.of(
                new ScrollGroup(body, top -> content(new Rect(body.x(), top, body.width(), 0))),
                new TextButton(Icon.line("check"), CommonComponents.GUI_DONE,
                        frame.actions(actionWidth(CommonComponents.GUI_DONE)).get(0), this::onClose)
                        .kind(Look.ButtonKind.PRIMARY));
    }

    @Override
    public void onClose() {
        Screens.show(editor.screen());
    }

    /** The modes on a box, the decorations on another, then each mod's structures on a box of their own. */
    private ScrollGroup.Laid content(Rect area) {
        FlowLayout flow = new FlowLayout(area, PanelSizes.ROW_HEIGHT, PanelSizes.GAP);
        List<Widget> widgets = new ArrayList<>(Surface.box(flow, this::modeToggles));
        widgets.addAll(Surface.box(flow, this::decorationToggles));
        Map<String, List<StructureSetInfo>> byMod = new LinkedHashMap<>();
        sets.forEach(set -> byMod.computeIfAbsent(set.namespace(), mod -> new ArrayList<>()).add(set));
        byMod.forEach((mod, modSets) -> widgets.addAll(Surface.box(flow, inside -> modBox(mod, modSets, inside))));
        return new ScrollGroup.Laid(widgets, flow.bottom());
    }

    /** The ways of choosing the structures as one segmented choice, only one chosen at a time. */
    private List<Widget> modeToggles(FlowLayout flow) {
        List<SegmentedChoice.Option> options = Arrays.stream(StructureChoice.Mode.values()).map(this::modeOption)
                .toList();
        return SegmentedChoice.layOut(options, flow.next(SegmentedChoice.width(options)));
    }

    /** How wide a switch is so as many as fit fill a row this wide, none narrower than a switch with a name. */
    private static int columnWidth(int rowWidth) {
        int columns = Math.max(1, (rowWidth + PanelSizes.GAP) / (PanelSizes.DOUBLE_WIDTH + PanelSizes.GAP));
        return (rowWidth - (columns - 1) * PanelSizes.GAP) / columns;
    }

    /** A mod's name under its first structure's picture, then a switch for each of its structure sets. */
    private List<Widget> modBox(String mod, List<StructureSetInfo> modSets, FlowLayout flow) {
        List<Widget> widgets = new ArrayList<>();
        widgets.add(new Label(StructureIcons.of(modSets.get(0).id()), Component.literal(ClientPlatform.get()
                .modName(mod)), flow.line(Label.height())));
        int width = columnWidth(flow.rest().width());
        modSets.forEach(set -> widgets.add(setToggle(set, flow.next(width))));
        return widgets;
    }

    /** The switches for the biome's decorations and for the lakes. */
    private List<Widget> decorationToggles(FlowLayout flow) {
        return List.of(
                new Toggle(Icon.item("minecraft:poppy"), Component.translatable("flatgen.structures.decorations"),
                        flow.next(PanelSizes.DOUBLE_WIDTH), () -> preset().decorations(),
                        () -> editor.apply(new SetDecorations(!preset().decorations(), preset().lakes())),
                        () -> true, tip("flatgen.structures.decorations_tip")),
                new Toggle(Icon.item("minecraft:lava_bucket"), Component.translatable("flatgen.structures.lakes"),
                        flow.next(PanelSizes.DOUBLE_WIDTH), () -> preset().lakes(),
                        () -> editor.apply(new SetDecorations(preset().decorations(), !preset().lakes())),
                        () -> true, tip("flatgen.structures.lakes_tip")));
    }

    private SegmentedChoice.Option modeOption(StructureChoice.Mode mode) {
        Component name = Component.translatable(modeName(mode));
        return new SegmentedChoice.Option(MODE_ICONS.get(mode), () -> name, textWidth(name, TextStyle.STRONG),
                () -> StructureChoice.mode(preset().structureSets()) == mode,
                () -> editor.apply(new SetStructures(StructureChoice.withMode(preset().structureSets(), mode, every))),
                tip(modeTip(mode)));
    }

    /** A way of choosing structures' name, by its translation key. */
    private static String modeName(StructureChoice.Mode mode) {
        return switch (mode) {
            case BIOME -> "flatgen.structures.mode.biome";
            case NONE -> "flatgen.structures.mode.none";
            case CHOSEN -> "flatgen.structures.mode.chosen";
        };
    }

    /** What a way of choosing structures does, by its translation key. */
    private static String modeTip(StructureChoice.Mode mode) {
        return switch (mode) {
            case BIOME -> "flatgen.structures.mode.biome_tip";
            case NONE -> "flatgen.structures.mode.none_tip";
            case CHOSEN -> "flatgen.structures.mode.chosen_tip";
        };
    }

    /** A set's switch: locked on when an option adds it, struck through when it cannot appear in the biome. */
    private Toggle setToggle(StructureSetInfo set, Rect place) {
        boolean appears = set.appearsIn(preset().biome());
        Icon icon = appears ? StructureIcons.of(set.id()) : Icon.struck(StructureIcons.of(set.id()));
        return new Toggle(icon, StructureNames.of(set), place,
                () -> fromOptions.contains(set.id()) || StructureChoice.isOn(preset().structureSets(), set.id()),
                () -> editor.apply(new SetStructures(StructureChoice.toggled(preset().structureSets(), set.id(),
                        every))),
                () -> !fromOptions.contains(set.id()), () -> describe(set, appears));
    }

    private List<Component> describe(StructureSetInfo set, boolean appears) {
        List<Component> lines = new ArrayList<>(List.of(Component.literal(set.id())));
        if (fromOptions.contains(set.id())) {
            lines.add(Component.translatable("flatgen.structures.from_option"));
        }
        if (!appears) {
            lines.add(Component.translatable("flatgen.structures.not_in_biome", Names.biome(preset().biome()),
                    set.biomes().stream().sorted().limit(BIOMES_NAMED).map(Names::biome)
                            .map(Component::getString).collect(Collectors.joining(", "))));
        }
        return lines;
    }

    private Preset preset() {
        return editor.shown().preset();
    }

    private static Supplier<List<Component>> tip(String key) {
        return () -> List.of(Component.translatable(key));
    }
}
