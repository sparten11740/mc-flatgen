# F28 Java API for world options

Milestone: after v1 · Release: v1.x · Status: todo · Depends on: F27, F20

## Why

Some options need code: settings of their own beyond on and off (an airport's size, how many launch pads), placing that
depends on the world, or templates (F14) another mod provides. JSON options (F27) cannot express these.

## What the player gets

Options and templates from other mods that go beyond a switch, in the same panels as Flatgen's own.

## Acceptance criteria

- [ ] A separate `flatgen-api` jar, one for every node, that mods compile against (`compileOnly`), with semantic
  versions as Blockpilot's API has: a minor version for additions, a major one for breaking changes.
- [ ] Mods register option effect kinds, options with parameters, and templates during mod loading, in a class only
  touched when Flatgen is installed.
- [ ] The API names no Minecraft class that differs between the versions Flatgen supports, so one jar serves all.
- [ ] Built only when a mod needs it: the first user (Skyliners or another) is named in this file before work starts.

## Design

- The API is the interfaces F27 and F14 already use internally (`Effect` kinds, `Template`), moved to their own module
  without change, so Flatgen's own options and templates stay its first users.
