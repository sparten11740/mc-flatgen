# F21 Exports for servers and datapacks

Milestone: after v1 · Release: v1.x · Status: todo · Depends on: F20

## Why

A preset made in Flatgen should also make a dedicated server's world, or appear as a World Type for friends without
Flatgen.

## What the player gets

Two more ways to share a preset: the lines to paste into a server's `server.properties`, and a datapack that adds the
preset as a World Type.

## Acceptance criteria

- [ ] `server.properties`: `level-type=minecraft\:flat` and `generator-settings=<JSON of the flat settings>`, escaped
  as the file needs, copied to the clipboard.
- [ ] Datapack: a zip in `config/flatgen/exports/` with `pack.mcmeta` (the pack format of the node's version), the
  world preset JSON and the `#minecraft:normal` tag entry, so the World Type list shows it.
- [ ] Both checked by starting a dedicated server with them (GameTest server or boot tool).

## Version notes

- The pack format and the world preset JSON differ per version; both come from the version tree (`mc/Exports`).

## Tests

- Unit: escaping; the datapack's files.
- Boot: a dedicated server with the exported settings generates the preset's layers.
