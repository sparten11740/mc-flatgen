# F27 World options from other mods

Milestone: M2 biome and world · Release: v1 · Status: todo · Depends on: F02, F01

## Why

Flatgen should be where a modpack sets up its flat worlds, and other mods know best what their worlds want: Skyliners
an airport at spawn, a rocket mod a launch pad, an airship mod no structures and clear skies. A world option is a
switch in Flatgen's editor, with a picture, that does something to the world being created. Mods and packs add their
own as data, with no code and no dependency on Flatgen; Flatgen's own switches (always day, always clear, mobs) are
options in the same format, so the format is proven by its first user.

## What the player gets

A panel of picture switches grouped by mod ("Time and weather", "Skyliners", ...). Turning one on is part of the
preset, saved with it like its layers.

## Acceptance criteria

- [ ] Options load from resources at `assets/<namespace>/flatgen/options/<name>.json`, from mods and resource packs;
  an option's id is `<namespace>:<name>`.
- [ ] An option has a name and description (translation keys or text), an icon (an item id), a group, a default, the
  mods it requires (hidden when one is missing) and a list of effects.
- [ ] Effects, each a kind of its own:
  - `game_rule`: sets a game rule on the Create World screen (always clear is `doWeatherCycle` false);
  - `first_start_time`: sets the clock once, on the world's first start (always day's midday);
  - `structures`: adds structure sets to the world's structure list;
  - `place_at_spawn`: places a structure template once, at or beside the spawn point, on the world's first start.
- [ ] A preset stores the options that differ from their defaults by id (F02's `options`); an option that is not
  installed is kept in the preset and reported, never dropped.
- [ ] A broken option file is logged with its path and what is wrong, and skipped; the editor still opens.
- [ ] Worlds still open without Flatgen: game rules and structure lists are vanilla settings, and first-start effects
  leave only blocks and the clock behind.
- [ ] `docs/world-options.md` for mod and pack authors: the format, each effect, examples (Skyliners' airport).
- [ ] Tested with a fixture option shipped in the dev run's resources that places a small template at spawn; the
  Skyliners airport itself is added in Skyliners' repository.

## Design

- `options/rules/WorldOption` (pure record) and `options/rules/OptionReader` (JSON to options, with problems listed).
- `options/rules/Effect`: one record per kind, read through a table of kinds by name; a new kind is a record, its
  reader entry and its applier, nothing else changes.
- Appliers in `options/`: what each kind does to the Create World screen (`game_rule`, `structures`, through `mc/`) and
  what it does on the first start (`first_start_time`, `place_at_spawn`).
- The first start: when Done applies options with first-start effects, Flatgen remembers them for the world about to
  be created; when the integrated server first starts that world (game time 0, marked), it runs them once. The server
  start hook is the one loader-specific piece: NeoForge's `ServerStartedEvent`, Fabric's
  `ServerLifecycleEvents.SERVER_STARTED`, behind `ClientPlatform`.
- The editor's options panel is built on a toggle list widget in the shared toolkit (`client/gui/ToggleList`), which
  the structures panel of F09 reuses.

## Version notes

- Game rule names and access change in later releases; `mc/GameRuleAccess` holds each version's.
- Placing a template uses the game's structure template manager, which `mc/` wraps.

## Tests

- Unit: reading every effect kind, defaults, missing keys, unknown kinds reported, required mods.
- GameTest: a `place_at_spawn` fixture places its template at the spawn point once and not on a second start; a
  `first_start_time` fixture sets the clock once.
- Dev client: the fixture option shows with its picture, is saved in a preset, and its world has the template at spawn.

## Out of scope

Options with logic beyond these effects, and templates (F14) from other mods: they need code, which comes with the Java
API of F28.
