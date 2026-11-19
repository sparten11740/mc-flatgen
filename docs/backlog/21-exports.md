# F21 Exports for servers and datapacks

Milestone: after v1 · Release: v1.x · Status: done · Depends on: F20

## Why

A preset made in Flatgen should also make a dedicated server's world, or appear as a World Type for friends without
Flatgen.

## What the player gets

Two more ways to share a preset: the lines to paste into a server's `server.properties`, and a datapack that adds the
preset as a World Type.

## Acceptance criteria

- [x] `server.properties`: `level-type=minecraft\:flat` and `generator-settings=<JSON of the flat settings>`, escaped
  as the file needs, copied to the clipboard.
- [x] Datapack: a zip in `config/flatgen/exports/` with `pack.mcmeta` (the pack format of the node's version), the
  world preset JSON and the `#minecraft:normal` tag entry, so the World Type list shows it.
- [x] Both checked by starting a dedicated server with them (GameTest server or boot tool).

## Design

- The library's second row has two more picture buttons for the picked preset: Server (a server rack, Flatgen's own
  picture) puts the lines on the clipboard, and Datapack (a bundle) writes `config/flatgen/exports/<name>.zip`,
  replacing an earlier export of that name, and opens the folder.
- Both make the world Done would: `OptionsAtDone.settings`, the preset's superflat settings with the structure sets its
  world options add, the parts the game lacks standing in as when the preset is used (the library puts them in and
  names the zip after the preset's file, so `exports` knows nothing of the library). Options' game rules and
  first-start effects are not part of a world's generator, so neither export carries them.
- `exports/rules` (pure, tested): `ServerProperties` writes the two lines, the value escaped as `java.util.Properties`
  reads it back; `DatapackFiles` lays out the pack: `pack.mcmeta` with the data pack format and `supported_formats`
  spanning the resource one too, the world preset at `data/flatgen/worldgen/world_preset/<name>.json`, the
  `#minecraft:normal` tag entry, and `assets/flatgen/lang/en_us.json` naming the World Type, which the game reads when
  the zip is also used as a resource pack (as a datapack alone, the World Type shows its key).
- `mc/Exports` holds what is the game's own: the settings and the world preset written with the game's codecs (the
  world preset is the Default world type's with its overworld superflat), and the version's pack formats.

## Version notes

- The pack format and the world preset JSON differ per version; both come from the version tree (`mc/Exports`).

## Tests

- Unit: the lines read back by `java.util.Properties`, escapes included (`ServerPropertiesTest`); the datapack's files
  (`DatapackFilesTest`).
- GameTest: the server lines' settings decode to the preset's layers, and the world preset decodes to the Default world
  type with the preset's overworld (`ExportGameTests`).

## Tried

NeoForge, in a dev client with Blockpilot: a preset of bedrock, three stone and glass saved in the library; Server put
`level-type=minecraft\:flat` and its `generator-settings` on the clipboard, and Datapack wrote `glass_test.zip` to the
exports folder, saying so. A NeoForge 21.1.1 dedicated server with no mods made a world of exactly those layers from
the lines, and another from the zip in its world's `datapacks` with `level-type=flatgen\:glass_test`. On Fabric the
same steps wrote the same generator settings and the same world preset. Not tried: the zip as a resource pack for the
World Type's name.
