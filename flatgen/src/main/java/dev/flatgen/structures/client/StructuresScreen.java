package dev.flatgen.structures.client;

import dev.flatgen.client.gui.FlatgenScreen;
import dev.flatgen.client.gui.Icon;
import dev.flatgen.client.gui.Label;
import dev.flatgen.client.gui.ScrollGroup;
import dev.flatgen.client.gui.TextButton;
import dev.flatgen.client.gui.Toggle;
import dev.flatgen.client.gui.Widget;
import dev.flatgen.client.gui.rules.FlowLayout;
import dev.flatgen.client.gui.rules.Rect;
import dev.flatgen.editor.client.panel.EditorContext;
import dev.flatgen.editor.client.panel.PanelSizes;
import dev.flatgen.mc.Names;
import dev.flatgen.platform.ClientPlatform;
import dev.flatgen.preset.rules.Preset;
import dev.flatgen.structures.rules.SetDecorations;
import dev.flatgen.structures.rules.SetStructures;
import dev.flatgen.structures.rules.StructureChoice;
import dev.flatgen.structures.rules.StructureSetInfo;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
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
    /** Space above the switches for the title, and below them for Done, in GUI pixels. */
    private static final int HEADER = 24;
    private static final int FOOTER = 32;
    private static final int MARGIN = 12;
    private static final int DONE_WIDTH = 100;
    /** The width of a switch, in GUI pixels: two of the panel's wide buttons and the gap between them. */
    private static final int TOGGLE_WIDTH = 2 * PanelSizes.WIDE_BUTTON + PanelSizes.GAP;
    /** How many of the biomes a set needs its tooltip names. */
    private static final int BIOMES_NAMED = 3;
    private static final Map<StructureChoice.Mode, Icon> MODE_ICONS = Map.of(
            StructureChoice.Mode.BIOME, Icon.item("minecraft:grass_block"),
            StructureChoice.Mode.NONE, Icon.item("minecraft:barrier"),
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
        Rect area = new Rect(MARGIN, HEADER, width - 2 * MARGIN, height - HEADER - FOOTER);
        return List.of(
                new ScrollGroup(area, top -> content(new Rect(MARGIN, top, area.width(), 0))),
                new TextButton(CommonComponents.GUI_DONE, new Rect((width - DONE_WIDTH) / 2,
                        height - (FOOTER + PanelSizes.ROW_HEIGHT) / 2, DONE_WIDTH, PanelSizes.ROW_HEIGHT),
                        this::onClose));
    }

    @Override
    public void onClose() {
        minecraft.setScreen(editor.screen());
    }

    /** The modes, the decorations and lakes, then each mod's structure sets, from the top of this area down. */
    private ScrollGroup.Laid content(Rect area) {
        FlowLayout flow = new FlowLayout(area, PanelSizes.ROW_HEIGHT, PanelSizes.GAP);
        List<Widget> widgets = new ArrayList<>();
        for (StructureChoice.Mode mode : StructureChoice.Mode.values()) {
            widgets.add(modeToggle(mode, flow.next(TOGGLE_WIDTH)));
        }
        flow.newRow();
        widgets.addAll(decorationToggles(flow));
        Map<String, List<StructureSetInfo>> byMod = new LinkedHashMap<>();
        sets.forEach(set -> byMod.computeIfAbsent(set.namespace(), mod -> new ArrayList<>()).add(set));
        for (Map.Entry<String, List<StructureSetInfo>> mod : byMod.entrySet()) {
            widgets.add(new Label(StructureIcons.of(mod.getValue().get(0).id()),
                    Component.literal(ClientPlatform.get().modName(mod.getKey())), flow.line(Label.height())));
            mod.getValue().forEach(set -> widgets.add(setToggle(set, flow.next(TOGGLE_WIDTH))));
            flow.newRow();
        }
        return new ScrollGroup.Laid(widgets, flow.bottom());
    }

    /** The switches for the biome's decorations and for the lakes. */
    private List<Toggle> decorationToggles(FlowLayout flow) {
        return List.of(
                new Toggle(Icon.item("minecraft:poppy"), Component.translatable("flatgen.structures.decorations"),
                        flow.next(TOGGLE_WIDTH), () -> preset().decorations(),
                        () -> editor.apply(new SetDecorations(!preset().decorations(), preset().lakes())),
                        () -> true, tip("flatgen.structures.decorations_tip")),
                new Toggle(Icon.item("minecraft:lava_bucket"), Component.translatable("flatgen.structures.lakes"),
                        flow.next(TOGGLE_WIDTH), () -> preset().lakes(),
                        () -> editor.apply(new SetDecorations(preset().decorations(), !preset().lakes())),
                        () -> true, tip("flatgen.structures.lakes_tip")));
    }

    private Toggle modeToggle(StructureChoice.Mode mode, Rect place) {
        String key = "flatgen.structures.mode." + mode.name().toLowerCase(Locale.ROOT);
        return new Toggle(MODE_ICONS.get(mode), Component.translatable(key), place,
                () -> StructureChoice.mode(preset().structureSets()) == mode,
                () -> editor.apply(new SetStructures(StructureChoice.withMode(preset().structureSets(), mode, every))),
                () -> true, tip(key + "_tip"));
    }

    /** A set's switch: locked on when an option adds it, struck through when it cannot appear in the biome. */
    private Toggle setToggle(StructureSetInfo set, Rect place) {
        boolean appears = set.appearsIn(preset().biome());
        Icon icon = appears ? StructureIcons.of(set.id()) : Icon.struck(StructureIcons.of(set.id()));
        return new Toggle(icon, Component.literal(set.name()), place,
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
