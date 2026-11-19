package dev.flatgen.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/**
 * What a widget says of itself to the game's narrator, in the game's own words for its kind of widget: what it is,
 * more about it, and how it is used, with the keyboard's focus on it or under the mouse. Widgets make one; only
 * {@link FlatgenScreen} hands it to the game.
 *
 * @param title        what it is, as "Button: Undo"
 * @param more         what else it says, such as its state, what it does or why it cannot be used
 * @param focusedUsage how it is used with the focus on it; none when there is nothing to say
 * @param hoveredUsage how it is used under the mouse; none when there is nothing to say
 */
public record Narration(Component title, List<Component> more, Optional<Component> focusedUsage,
        Optional<Component> hoveredUsage) {
    public Narration {
        more = List.copyOf(more);
    }

    /** A button of this name, with more to say, unusable now or not. */
    public static Narration button(Component name, boolean usable, List<Component> more) {
        return new Narration(Component.translatable("narration.button", name), unavailableUnless(usable, more),
                Optional.of(Component.translatable("narration.button.usage.focused")),
                Optional.of(Component.translatable("narration.button.usage.hovered")));
    }

    /** A button that shows a picture alone, named by its tooltip's first line, the rest said after. */
    public static Narration button(List<Component> tooltip, boolean usable) {
        return button(name(tooltip), usable, more(tooltip));
    }

    /** The name of a control that shows a picture alone: its tooltip's first line, or nothing. */
    public static Component name(List<Component> tooltip) {
        return tooltip.isEmpty() ? Component.empty() : tooltip.get(0);
    }

    /** What such a control says after its name: the rest of its tooltip. */
    public static List<Component> more(List<Component> tooltip) {
        return tooltip.isEmpty() ? List.of() : tooltip.subList(1, tooltip.size());
    }

    /** A switch of this name, on or off, with more to say, unusable now or not. */
    public static Narration toggle(Component name, boolean on, boolean usable, List<Component> more) {
        List<Component> said = new ArrayList<>(List.of(on ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF));
        said.addAll(unavailableUnless(usable, more));
        return new Narration(Component.translatable("narration.checkbox", name), said,
                Optional.of(Component.translatable("narration.checkbox.usage.focused")),
                Optional.of(Component.translatable("narration.checkbox.usage.hovered")));
    }

    /** A slider of this label, which says its value. */
    public static Narration slider(Component label) {
        return new Narration(Component.translatable("gui.narrate.slider", label), List.of(),
                Optional.of(Component.translatable("narration.slider.usage.focused")),
                Optional.of(Component.translatable("narration.slider.usage.hovered")));
    }

    /** A field of this name holding this text. */
    public static Narration field(Component name, String text) {
        return new Narration(Component.translatable("gui.narrate.editBox", name, text), List.of(), Optional.empty(),
                Optional.empty());
    }

    /** One of many entries, such as a card or a layer, with more to say; the arrows move to another. */
    public static Narration entry(Component name, List<Component> more) {
        return new Narration(name, more, Optional.of(Component.translatable("narration.selection.usage")),
                Optional.empty());
    }

    /** One of many entries described in lines, the first naming it and the rest said after. */
    public static Narration entry(List<Component> lines) {
        return entry(lines.get(0), lines.subList(1, lines.size()));
    }

    /** Something that is only said, such as a fact. */
    public static Narration said(List<Component> words) {
        return new Narration(CommonComponents.joinForNarration(words.toArray(Component[]::new)), List.of(),
                Optional.empty(), Optional.empty());
    }

    /** Tells the narrator, with the focus on the widget or with the mouse over it. */
    void tell(NarrationElementOutput output, boolean focused) {
        output.add(NarratedElementType.TITLE, title);
        if (!more.isEmpty()) {
            output.add(NarratedElementType.HINT, CommonComponents.joinForNarration(more.toArray(Component[]::new)));
        }
        (focused ? focusedUsage : hoveredUsage).ifPresent(usage -> output.add(NarratedElementType.USAGE, usage));
    }

    private static List<Component> unavailableUnless(boolean usable, List<Component> more) {
        List<Component> said = new ArrayList<>(more);
        if (!usable) {
            said.add(0, Component.translatable("flatgen.narration.unavailable"));
        }
        return said;
    }
}
