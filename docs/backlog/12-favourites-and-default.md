# F12 Favourites and default

Milestone: M3 presets · Release: v1 · Status: todo · Depends on: F11

## Why

Most players use two or three presets. Favourites put them one click away; a default means a new superflat world is
already the way the player likes it, without opening Customize at all.

## What the player gets

A star on each preset card; starred presets sit as a row of cards at the top of the editor. A pin makes one preset the
default: choosing Superflat applies it straight away. An option opens Create World on Superflat with the default
already applied.

## Acceptance criteria

- [ ] Star and unstar on the library's cards (a right click); favourites shown first in the library and as a row of
  pictures in the editor that uses one in a click.
- [ ] One default at most; pinning another moves the pin.
- [ ] When the World Type changes to Superflat and its settings are still vanilla's default flat settings, the
  default preset is applied (layers, biome, structures, decorations, lakes and world options). Settings the player has
  already changed are never replaced.
- [ ] Option "Start Create World on Superflat": the World Type is Superflat with the default applied when the screen
  opens.
- [ ] Favourites, the default and recently used presets live in `config/flatgen/library.json`, by preset file name,
  so preset files stay free of personal choices and can be shared as they are.

## Design

- `library/rules/Choices` (pure): favourites and recents (the pickers' `search/rules/Picks`), the default, "start on
  Superflat" (in library.json, as plan 3.5 has it), and when a default may be applied (the world's settings still the
  game's own default flat world).
- Watching the World Type uses `WorldCreationUiState.addListener`, a public method, from a screen-opened hook
  (`library/client/DefaultPresets`); the hook is the loader-specific piece: NeoForge's `ScreenEvent.Init.Post`, and on
  Fabric, which Flatgen uses without Fabric API, a mixin at the return of `Minecraft.setScreen`. As with F27's server
  start, the platform halves call the feature directly. A listener is added once per screen and guards against its
  own change; the default is applied as Done applies a preset, world options included, with parts the game lacks
  replaced and logged.

## Tests

- Unit: `Choices`, including "never replace changed settings".
- Dev client: set a default, open Create World, choose Superflat, create the world, check its layers; with the option
  on, the screen opens on Superflat.
