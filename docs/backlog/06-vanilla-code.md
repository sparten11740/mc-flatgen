# F06 Vanilla superflat code

Milestone: M1 layer editor · Release: v1 · Status: done · Depends on: F02

## Why

Players share superflat worlds as the vanilla code (`minecraft:bedrock,2*minecraft:dirt,minecraft:grass_block;
minecraft:plains`), and many presets on the web are written that way. Flatgen reads and writes it, and does better than
vanilla at saying what is wrong: vanilla silently falls back to the default world when one block id is unknown.

## What the player gets

Copy code puts the current layers and biome on the clipboard as the game's code; Paste code turns a code on the
clipboard into layers at once, or says which part is wrong and changes nothing. Two buttons rather than a text box, as
Flatgen's fields have no selection or clipboard of their own.

## Acceptance criteria

- [x] Writes the code for the current layers and biome exactly as vanilla's presets screen does.
- [x] Reads any code vanilla reads: `<layers>;<biome>`, layers as `[<height>*]<block>` separated by commas, the biome
  optional (plains when missing), spaces around parts allowed.
- [x] A layer it cannot read (a bad number, a malformed id), no layers, an unknown block or biome, or a stack taller
  than the world is explained under the selected layer, naming the part and its characters; nothing is applied until
  the code is valid.
- [x] Pasting a code is one undoable edit (F04).
- [x] Structures, decorations and lakes, which the code does not carry, stay as they were.

## Design

- `preset/rules/VanillaCode` (pure): writing came with F26; this feature adds reading, to layers, a biome and the
  problems found, each a kind with the part's text and characters, which the editor words through translations.
- `editor/client/CodeChecks` finds the unknown blocks and biome of a code read; `layers/rules/LayerEdits.Replace` puts
  the code's layers and biome in, one step; `mc/Clipboard` reaches the clipboard.
- The game's parser (`PresetFlatWorldScreen`) is a client class, not on the GameTest server, so the GameTest compares
  against the text the game writes for its layers (`FlatLayerInfo`), which its writer joins.

## Version notes

- The format has been the same since 1.13; check 26.1's parser in the port.

## Tests

- Unit: `VanillaCodeTest` (writing, reading back, spaces and a missing biome, unreadable layers with their characters,
  no layers), `LayerEditsTest` (replacing, refused when taller than the world).
- GameTest on both nodes: every game preset's code is written as the game writes its layers and reads back to the same
  layers and biome.
- Played with Blockpilot: on NeoForge, Copy code put the classic flat's code on the clipboard; pasting the tall flat
  made its layers; a broken layer, an unknown block and a stack of 400 changed nothing and said why; Undo took the
  paste back. On Fabric, Water World's code pasted, and a broken one was refused with its message.
