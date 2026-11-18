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

- [ ] Star and unstar on cards and in the editor; favourites shown first in the library and as a row in the editor.
- [ ] One default at most; pinning another moves the pin.
- [ ] When the World Type changes to Superflat and its settings are still vanilla's default flat settings, the
  default preset is applied (layers, biome, structures, decorations, lakes and game rules). Settings the player has
  already changed are never replaced.
- [ ] Option "Start Create World on Superflat": the World Type is Superflat with the default applied when the screen
  opens.
- [ ] Favourites, the default and recently used presets live in `config/flatgen/library.json`, by preset file name,
  so preset files stay free of personal choices and can be shared as they are.

## Design

- `library/rules/Choices` (pure): favourites, default, recents, and when a default may be applied.
- Watching the World Type uses `WorldCreationUiState.addListener`, a public method, from a screen-opened hook; the
  hook is the loader-specific piece: NeoForge's `ScreenEvent.Init.Post`, Fabric's `ScreenEvents.AFTER_INIT`, both
  behind `ClientPlatform.onScreenOpened`. A listener runs once per screen and guards against its own change.

## Tests

- Unit: `Choices`, including "never replace changed settings".
- Dev client: set a default, open Create World, choose Superflat, create the world, check its layers; with the option
  on, the screen opens on Superflat.
