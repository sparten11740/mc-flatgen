# F27 World options from other mods

Milestone: M2 biome and world · Release: v1 · Status: done · Depends on: F02, F01

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

- [x] Options load from resources at `assets/<namespace>/flatgen/options/<name>.json`, from mods and resource packs;
  an option's id is `<namespace>:<name>`.
- [x] An option has a name and description (translation keys or text), an icon (an item id), a group, a default, the
  mods it requires (hidden when one is missing) and a list of effects.
- [x] Effects, each a kind of its own:
  - `game_rule`: sets a game rule on the Create World screen (always clear is `doWeatherCycle` false);
  - `first_start_time`: sets the clock once, on the world's first start (always day's midday);
  - `structures`: adds structure sets to the world's structure list;
  - `place_at_spawn`: places a structure template once, at or beside the spawn point, on the world's first start.
- [x] A preset stores the options that differ from their defaults by id (F02's `options`); an option that is not
  installed is kept in the preset and reported, never dropped.
- [x] A broken option file is logged with its path and what is wrong, and skipped; the editor still opens.
- [x] Worlds still open without Flatgen: game rules and structure lists are vanilla settings, and first-start effects
  leave only blocks and the clock behind.
- [x] `docs/world-options.md` for mod and pack authors: the format, each effect, examples (Skyliners' airport).
- [x] Tested with a fixture option shipped in the dev run's resources that places a small template at spawn; the
  Skyliners airport itself is added in Skyliners' repository.

## Design

- `options/rules/WorldOption` (pure record) and `options/rules/OptionReader` (JSON to options, with problems listed).
- `options/rules/Effect`: one record per kind, read through a table of kinds by name; a new kind is a record, its
  reader entry and its applier, nothing else changes.
- Appliers in `options/`: what each kind does to the Create World screen (`game_rule`, `structures`, through `mc/`) and
  what it does on the first start (`first_start_time`, `place_at_spawn`).
- The format (decided by a panel with a pack author's view, 2026-11-18): `icon` (an item id, or
  `{"texture": "<ns>:textures/....png"}`, 16 by 16) and `effects` are required; `name` and `description` default to the
  keys `option.<ns>.<name>` and `option.<ns>.<name>.description` and are read as a translation key or else as the text
  itself; `group` defaults to the namespace (a mod's id takes the mod's name, `<ns>:<path>` the key
  `option_group.<ns>.<path>`), a group's picture is its first option's; `default` false; `requires` mod ids; `format`
  1. Each effect is an object with a `type`. `place_at_spawn` stands the template on the first block above the top
  layer at the spawn's column (`void_height`, 64, with no layers), `align` `center` or `corner`, then `offset`, a
  vanilla `rotation`, and an optional `spawn` block of the template the world's spawn moves to. Options that fix the
  same game rule or clock differently cannot be on together: turning one on turns the others off. Templates are server
  data (`data/<ns>/structure/`), from mod jars and data packs; resource packs cannot add them.
- The first start (decided by a panel with an engineer's view, 2026-11-18): Done builds the overworld's
  `FlatLevelSource` itself, puts that object on the Create World screen and keeps it with the first-start effects. A
  listener added through the screen's public `WorldCreationUiState.addListener` follows a data-pack change, which
  rebuilds the generator with the same settings. At every integrated server start Flatgen takes what it kept and runs
  the effects only if the server's overworld generator is that very object, which no other world can have: a world
  created after a Cancel, reopened, or of another session has its own. Nothing is left in the world, no marker or
  game-time test is needed. The hooks: NeoForge's `ServerStartedEvent`; on Fabric, without Fabric API, a mixin at the
  return of `IntegratedServer.initServer`; both call the feature directly. There the overworld, its spawn, the spawn
  chunks and the template manager are ready and no player has joined.
- The switches chosen for a Create World screen are remembered for it, so the editor reopens with them, and an option
  turned off since the last Done puts its game rules back to the game's defaults.
- The editor's options panel lays out picture switches (`client/gui/Toggle`) under each group's heading; F09's
  structures panel reuses the switch.

## Version notes

- Game rule names and access change in later releases; `mc/GameRuleAccess` holds each version's.
- Placing a template uses the game's structure template manager, which `mc/` wraps.

## Tests

- Unit: reading every effect kind, defaults, missing keys, unknown kinds reported, required mods.
- GameTest: a `place_at_spawn` fixture places its template at the spawn point once and not on a second start; a
  `first_start_time` fixture sets the clock once.
- Dev client: the fixture option shows with its picture, is saved in a preset, and its world has the template at spawn.

## Tried

Both loaders, in dev clients with Blockpilot: the fixture option shows with its picture under its group, turns on and
off as a step to undo, and its world has the pad at the spawn, noon and the daylight cycle off; Done, Cancel and a new
world leaves the new world without them. The GameTests run the first-start effects once and never on another world.
Not tried in game: the `structures` effect (unit-tested), texture icons, options from a resource pack, a data pack
change on the Create World screen after Done, a required mod that is missing, and opening such a world without Flatgen
(nothing of Flatgen is in it by design).

## Out of scope

Options with logic beyond these effects, and templates (F14) from other mods: they need code, which comes with the Java
API of F28.
