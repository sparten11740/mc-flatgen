package dev.flatgen.options.rules;

/** What effects may do once, on the world's first start. */
public interface FirstStart {
    /** Sets the time of day, in ticks from sunrise. */
    void setTime(long dayTime);

    /** Places a template at the world's spawn as the effect says. */
    void placeAtSpawn(PlaceAtSpawn placement);
}
