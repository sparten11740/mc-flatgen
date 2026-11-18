package dev.flatgen.compat.blockpilot.rules;

import java.util.Map;

/**
 * One row of a Flatgen command's report, as Blockpilot lists rows under a reply.
 *
 * @param key    the row's key, which a play test names it by
 * @param fields the row's fields by name, in the order they are said
 */
public record ReportRow(String key, Map<String, Object> fields) {
}
