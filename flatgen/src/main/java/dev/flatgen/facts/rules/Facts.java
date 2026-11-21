package dev.flatgen.facts.rules;

import dev.flatgen.pictures.rules.Picture;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Stream;

/** Every world fact, in the order shown; a new fact is a class here. */
public final class Facts {
    /**
     * The lines' colours and pictures: where slimes stop spawning, and where rain turns to snow; set before the facts
     * below, which read them as they are made.
     */
    private static final int SLIME_GREEN = 0xFF7FD34F;
    private static final int SNOW_BLUE = 0xFF8FD8FF;
    private static final Picture SLIME = Picture.texture("flatgen:textures/gui/sprites/icon/slime.png");
    private static final Picture SNOW = Picture.texture("flatgen:textures/gui/sprites/icon/snow.png");
    private static final Picture RAIN = Picture.texture("flatgen:textures/gui/sprites/icon/rain.png");
    /** The facts that differ from biome to biome, which the biome picker's cards show too. */
    public static final List<Fact> ABOUT_THE_BIOME = List.of(new Weather(), new Slimes(), new Monsters());
    /** Every fact, the surface's height first. */
    public static final List<Fact> ALL = Stream.concat(Stream.of(new Surface()), ABOUT_THE_BIOME.stream()).toList();

    private Facts() {
    }

    /** The Y mobs stand at, a ladder, as a template's ground at a Y has, with the number in words. */
    static final class Surface implements Fact {
        @Override
        public Shown show(Situation situation) {
            return new Shown(Picture.item("minecraft:ladder"), false,
                    List.of(new Words("flatgen.facts.surface", List.of(situation.facts().surfaceY()))));
        }
    }

    /** What falls at the surface: the sun for nothing, a rain cloud for rain, a snow cloud for snow. */
    static final class Weather implements Fact {
        private static final Map<WorldFacts.Weather, Picture> ICONS = Map.of(
                WorldFacts.Weather.NONE, Picture.item("minecraft:sunflower"),
                WorldFacts.Weather.RAIN, RAIN,
                WorldFacts.Weather.SNOW, SNOW);

        @Override
        public Shown show(Situation situation) {
            WorldFacts facts = situation.facts();
            List<Words> words = new ArrayList<>();
            words.add(Words.of(words(facts.weather())));
            facts.snowLine().ifPresent(y -> words.add(new Words("flatgen.facts.snow_line", List.of(y))));
            return new Shown(ICONS.get(facts.weather()), false, words);
        }

        /** A weather's words, by their translation key; the compiler holds every weather to having some. */
        private static String words(WorldFacts.Weather weather) {
            return switch (weather) {
                case NONE -> "flatgen.facts.weather.none";
                case RAIN -> "flatgen.facts.weather.rain";
                case SNOW -> "flatgen.facts.weather.snow";
            };
        }

        /** Where the biome's rain turns to snow, when it does. */
        @Override
        public Optional<Marker> marker(Situation situation) {
            OptionalInt line = WorldFacts.snowLine(situation.biome().climate());
            return line.isPresent() ? Optional.of(new Marker(line.getAsInt(), SNOW_BLUE, SNOW)) : Optional.empty();
        }
    }

    /** Whether slimes spawn, in slime chunks or on a swamp's surface; struck through when they do not. */
    static final class Slimes implements Fact {
        @Override
        public Shown show(Situation situation) {
            WorldFacts facts = situation.facts();
            List<Words> words = new ArrayList<>();
            if (facts.slimeChunks()) {
                words.add(new Words("flatgen.facts.slime_chunks", List.of(WorldFacts.SLIME_CHUNK_MAX_Y)));
            }
            if (facts.surfaceSlimes()) {
                words.add(Words.of("flatgen.facts.surface_slimes"));
            }
            boolean none = words.isEmpty();
            if (none) {
                words.add(Words.of("flatgen.facts.no_slimes_here"));
            }
            return new Shown(SLIME, none, words);
        }

        /** Where slimes stop spawning in slime chunks, when slimes spawn in the biome at all. */
        @Override
        public Optional<Marker> marker(Situation situation) {
            boolean slimes = situation.mobSpawning() && situation.biome().spawns().slimes();
            return slimes ? Optional.of(new Marker(WorldFacts.SLIME_CHUNK_MAX_Y, SLIME_GREEN, SLIME))
                    : Optional.empty();
        }
    }

    /** Whether monsters spawn, a creeper's head; struck through when they do not. */
    static final class Monsters implements Fact {
        @Override
        public Shown show(Situation situation) {
            boolean monsters = situation.facts().hostileMobs();
            return new Shown(Picture.item("minecraft:creeper_head"), !monsters,
                    List.of(Words.of(monsters ? "flatgen.facts.monsters" : "flatgen.facts.no_monsters")));
        }
    }
}
