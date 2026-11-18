package dev.flatgen.options.rules;

import com.google.gson.JsonObject;
import dev.flatgen.json.rules.JsonFields;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * {@code first_start_time}: sets the clock once, on the world's first start.
 *
 * @param time the time of day, in ticks from sunrise: 6000 is noon, 13000 nightfall
 */
public record FirstStartTime(long time) implements Effect {
    /** How long a day is, in ticks; a time of day is less. */
    private static final int DAY = 24000;
    /** The name of the clock as a setting that options may clash over. */
    private static final String SETTING = "first_start_time";

    /** Reads {@code {"time": 6000}}. */
    static Optional<Effect> read(JsonObject effect, List<String> problems) {
        int time = JsonFields.integer(effect, "time", -1, problems);
        if (time < 0 || time >= DAY) {
            problems.add("a first_start_time needs a time from 0 to " + (DAY - 1) + " ticks");
            return Optional.empty();
        }
        return Optional.of(new FirstStartTime(time));
    }

    @Override
    public void atFirstStart(FirstStart start) {
        start.setTime(time);
    }

    @Override
    public boolean actsAtFirstStart() {
        return true;
    }

    @Override
    public Map<String, String> settings() {
        return Map.of(SETTING, Long.toString(time));
    }
}
