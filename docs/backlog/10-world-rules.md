# F10 Time, weather and mob options

Milestone: M2 biome and world · Release: v1 · Status: todo · Depends on: F27

## Why

The test instance's command history is mostly `/time set day` and `/weather clear`, typed again in every new
test world. A preset that carries these settings saves that every time. They are Flatgen's own world options (F27),
in the format other mods use, so this feature is also the format's first real test.

## What the player gets

Three switches in Flatgen's options panel, each with a picture: always day, always clear weather, no mobs. They
become part of the preset and show under Create World → More → Game Rules as they are set.

## Acceptance criteria

- [ ] `assets/flatgen/flatgen/options/`: `always_clear` (`game_rule` `doWeatherCycle` false), `always_day`
  (`game_rule` `doDaylightCycle` false and `first_start_time` 6000), `no_mobs` (`game_rule` `doMobSpawning` false).
- [ ] A new world with always clear starts clear and stays clear; with always day it is midday on the first start and
  stays so; with no mobs nothing spawns on its own.
- [ ] Applied to the Create World screen's game rules when Done is pressed (and when a default preset is applied,
  F12); visible and changeable under Game Rules afterwards, which stays the last word.
- [ ] Opening Flatgen again shows the switches as the Create World screen's game rules have them.
- [ ] The clock is set once, on the world's first start, and never on later loads.

## Design

- Only data: the three option files and their translations. Everything they do is F27's effects.
- The game rule names in the files are the option format's; `mc/GameRuleAccess` maps them to each version's rules.

## Version notes

- Game rules are reworked in later releases (check their names and access in each port, 1.21.11 and 26.1 first);
  `GameRuleAccess` holds each version's way, so the option files stay the same everywhere.

## Tests

- GameTest: each option's effects applied to a level give the game rules and clock above.
- Dev client: always day and always clear on, create the world, wait a minute, still noon and clear; reload the world,
  the clock has moved on from where it stopped rather than back to midday.
