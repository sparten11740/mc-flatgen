# F06 Vanilla superflat code

Milestone: M1 layer editor · Release: v1 · Status: todo · Depends on: F02

## Why

Players share superflat worlds as the vanilla code (`minecraft:bedrock,2*minecraft:dirt,minecraft:grass_block;
minecraft:plains`), and many presets on the web are written that way. Flatgen reads and writes it, and does better than
vanilla at saying what is wrong: vanilla silently falls back to the default world when one block id is unknown.

## What the player gets

A box with the current preset's code to copy, and pasting a code turns it into layers at once, with the part that is
wrong marked.

## Acceptance criteria

- [ ] Writes the code for the current layers and biome exactly as vanilla's presets screen does.
- [ ] Reads any code vanilla reads: `<layers>;<biome>`, layers as `[<height>*]<block>` separated by commas, the biome
  optional (plains when missing).
- [ ] An unknown block or biome, a bad number or a stack taller than the world is marked in the box and explained;
  nothing is applied until the code is valid.
- [ ] Pasting a code is one undoable edit (F04).
- [ ] Structures, decorations and lakes, which the code does not carry, stay as they were.

## Design

- `preset/rules/VanillaCode` (pure): writing came with F26 (Blockpilot's `flatgen:editor` reports the code); this
  feature adds parsing to a preset part plus a list of problems with their character ranges.
- 1.21.1's own parser (`PresetFlatWorldScreen.fromString`) and writer (`save`) are the reference: the round-trip
  GameTest compares against them.

## Version notes

- The format has been the same since 1.13; check 26.1's parser in the port.

## Tests

- Unit: every vanilla preset's code, codes with spaces, missing biome, heights over the world, unknown ids, empty
  layers (vanilla treats an empty layer list as invalid).
- GameTest: for each vanilla preset, Flatgen's code equals `PresetFlatWorldScreen.save`, and both parsers read it to
  the same settings.
