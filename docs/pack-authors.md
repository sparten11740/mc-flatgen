# Flatgen for pack authors

A modpack or a resource pack can give its players superflat presets of its own and choose which preset Superflat
starts with, with no code: Flatgen reads data files, and a pack without Flatgen simply never reads them. World options
(switches such as "always day" or "an airport at the spawn") are explained in [world-options.md](world-options.md).

## Presets

Each preset is one JSON file in the game's resources, from a mod jar or a resource pack:

```
assets/<namespace>/flatgen/presets/<name>.json
```

The library shows it among the built-in presets, with a book on its card: players can use it and copy it into their
own presets, but not rename, change or delete it. Its key, which the library and `library.json` name it by, is
`pack:<namespace>:<name>`. A file in a resource pack higher up the list replaces one of the same key, as the game's own
resources do.

```json
{
  "format": 1,
  "name": "preset.skyislands.start",
  "icon": "minecraft:grass_block",
  "layers": [
    { "block": "minecraft:bedrock", "height": 1 },
    { "block": "minecraft:stone", "height": 60 },
    { "block": "minecraft:dirt", "height": 3 },
    { "block": "minecraft:grass_block", "height": 1 }
  ],
  "biome": "minecraft:plains",
  "structures": ["minecraft:villages"],
  "decorations": true,
  "lakes": false,
  "options": { "flatgen:always_day": true }
}
```

| Key | Default | What it is |
|---|---|---|
| `layers` | none | The layers from the bottom up, each a block id and how many blocks deep. Together no taller than the world (384 blocks in the overworld). |
| `name` | empty | The name on its card: a translation key from the pack's language files, or else the name itself. |
| `icon` | its top layer's block | The item its card shows, by id. |
| `biome` | `minecraft:plains` | The biome of the whole world. |
| `structures` | what the biome allows | The structure sets that generate, by id; `[]` for none. Left out, the world has every structure its biome allows. |
| `decorations` | `false` | Whether the biome's trees, flowers and ores generate. |
| `lakes` | `false` | Whether lakes generate. |
| `options` | none | World options set otherwise than their default, by id (see world-options.md). |
| `format` | `1` | The version of this format; a file of a later one is read as far as this Flatgen knows it. |

The easiest way to make one is in the game: build it in Flatgen's editor, save it, and copy the file from
`config/flatgen/presets/`. A preset that names a block, biome or structure set the game lacks still shows; using it
asks the player first and stands stone, plains or nothing in for what is missing.

## The default and the library's settings

A pack chooses what Superflat starts with in `config/flatgen/library.json`, the file Flatgen keeps the player's
choices in:

```json
{
  "default": "pack:skyislands:start",
  "start_on_superflat": true,
  "built_ins": "no_game",
  "starred": ["pack:skyislands:start"]
}
```

| Key | Default | What it is |
|---|---|---|
| `default` | none | The preset Superflat starts with on a new world: a pack preset's key, a game preset's (`game:minecraft:classic_flat`), or a file name of `config/flatgen/presets/`. |
| `start_on_superflat` | `false` | Whether Create World opens with Superflat chosen, the default ready. |
| `built_ins` | `shown` | Which built-in presets the library shows: `shown` (all), `no_game` (all but the game's own), or `hidden` (none). |
| `starred` | none | The presets starred as favourites, by key; the editor shows them as a row of pictures. |
| `recent` | none | The presets used lately, by key, newest first. |

Keys left out take their defaults, and unknown keys are ignored. The default is applied only to a new world's
Create World screen whose Superflat settings are still the game's own, never to a world being re-created.

Flatgen does not write the file until the player changes a setting, stars a preset or uses one; then it writes it with
their choices, so it is theirs from then on. Ship it the way your launcher ships default config files that an update
must not overwrite, or players lose their favourites and default when the pack updates.

A pack may also put presets the player can change in `config/flatgen/presets/`, the player's own folder: they show as
the player's own, editable and deletable.

## A pack's files

```
mods/skyislands-presets.jar          (or resourcepacks/skyislands.zip)
  assets/skyislands/flatgen/presets/start.json
  assets/skyislands/lang/en_us.json  ("preset.skyislands.start": "Sky islands")
config/flatgen/library.json
```

## Trying it

Open Create World, choose Superflat, press Customize, then the bookshelf: the preset shows among the built-ins. If it
does not, the game's log (`logs/latest.log`) names the file and what is wrong with it; a card with a barrier says in
its tooltip that it could not be read in full.
