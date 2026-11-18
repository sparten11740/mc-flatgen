# F10 Time, weather and mob settings

Milestone: M2 biome and world · Release: v1 · Status: todo · Depends on: F02, F01

## Why

The test instance's command history is mostly `/time set day` and `/weather clear`, typed again in every new
test world. A preset that carries these settings saves that every time.

## What the player gets

Three switches in Flatgen, each with a picture: always day, always clear weather, mobs spawn. They become part of the
preset and show under Create World → More → Game Rules as they are set.

## Acceptance criteria

- [ ] Always clear: `doWeatherCycle` off; a new world starts clear and stays clear.
- [ ] Always day: `doDaylightCycle` off and the clock at midday (6000) when the world first starts.
- [ ] Mobs spawn: `doMobSpawning`.
- [ ] Applied to the Create World screen's game rules when Done is pressed (and when a default preset is applied,
  F12); visible and changeable under Game Rules afterwards, which stays the last word.
- [ ] Opening Flatgen again shows the switches as the Create World screen's game rules have them.
- [ ] Setting the clock happens once, on the world's first start, and never on later loads.

## Design

- `rules/` naming is taken by pure logic, so this feature's package is `gamerules/`: `gamerules/rules/WorldRules`
  (pure record of the three switches) and `mc/GameRuleAccess` (version tree) to read and write
  `WorldCreationUiState.getGameRules()`.
- The switches are drawn with the toolkit's `client/gui/ToggleList`, made for F09.
- The clock: when Done applies "always day", Flatgen remembers it for the world about to be created
  (`gamerules/FirstStart`); on the integrated server's start, a level whose game time is 0 with that mark gets its
  day time set to 6000. The server-start hook is the one loader-specific piece: NeoForge's `ServerStartedEvent`,
  Fabric's `ServerLifecycleEvents.SERVER_STARTED`, both behind `Platform.onServerStarted`.

## Version notes

- Game rules are reworked in later releases (check their names and access in each port, 1.21.11 and 26.1 first);
  the version tree's `GameRuleAccess` holds each version's way, so shared code never names a game rule.

## Tests

- Unit: `WorldRules` to and from the preset.
- GameTest: a level with the mark gets day time 6000 on first start and keeps its time on a second start.
- Dev client: always day and always clear on, create the world, wait a minute, still noon and clear.
