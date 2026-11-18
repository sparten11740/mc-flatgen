package dev.flatgen.client.gui.rules;

/**
 * What a widget says of itself to tools that drive the screen, such as Blockpilot's {@code flatgen:widgets}.
 *
 * @param kind   what sort of widget it is, as {@code button} or {@code number}
 * @param label  what it is labelled, or its tooltip's first line when it has no label of its own
 * @param bounds where it is
 * @param active whether it can be used now
 * @param value  what it holds, empty for a widget that holds nothing
 */
public record WidgetDescription(String kind, String label, Rect bounds, boolean active, String value) {
}
