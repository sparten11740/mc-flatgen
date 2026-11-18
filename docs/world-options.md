# World options for mod and pack authors

A world option is a switch in Flatgen's superflat editor, with a picture, that does something to the world being
created: set a game rule, set the clock on the first start, add structure sets, or place a structure at the spawn. Mods
and packs add their own as data files, with no code and no dependency on Flatgen: without Flatgen the file is simply
never read. A player turns an option on or off in the editor, and a preset saves the choice with its layers.

## Where the files go

Each option is one JSON file in the game's resources, from a mod jar or a resource pack:

```
assets/<namespace>/flatgen/options/<name>.json
```

Its id is `<namespace>:<name>`; presets name options by it. A file in a resource pack higher up the list replaces one
of the same id, as the game's own resources do.

Templates placed at the spawn are server data and live where the game keeps structures: a mod jar or a data pack's
`data/<namespace>/structure/<name>.nbt`. A resource pack cannot add a template.

## The file

```json
{
  "format": 1,
  "name": "option.skyliners.airport",
  "description": "option.skyliners.airport.description",
  "icon": "skyliners:airliner",
  "group": "skyliners",
  "default": false,
  "requires": ["skyliners"],
  "effects": [ ... ]
}
```

| Key | Required | Default | What it is |
|---|---|---|---|
| `icon` | yes | | The switch's picture: an item id such as `minecraft:clock`, or `{"texture": "<namespace>:textures/....png"}`, a 16 by 16 texture, for things that are not items. |
| `effects` | yes | | What the option does, at least one effect (below). |
| `name` | no | `option.<namespace>.<name>` | Its name on the switch: a translation key, or else the text itself. |
| `description` | no | `option.<namespace>.<name>.description` | What it does, shown when the switch is hovered: a translation key, or else the text itself. |
| `group` | no | the namespace | The heading it is shown under. A mod's id shows the mod's name; `<namespace>:<path>` shows the translation of `option_group.<namespace>.<path>`. A group's picture is its first option's icon. |
| `default` | no | `false` | Whether the option is on unless a preset says otherwise. |
| `requires` | no | none | Mod ids; the option is hidden while one of them is missing, for packs that ship options for other mods. |
| `format` | no | `1` | The version of this format. A file of a later format than the Flatgen reading it is left out. |

Every player reads the switches by their pictures first, as some cannot read yet, so pick an icon that shows what the
option does.

## Effects

Each effect is an object with a `type`. An option whose effects cannot all be read is left out whole, never half
applied, and the game's log says which file and what is wrong.

### `game_rule`

Sets a game rule on the Create World screen, as the screen's own game rules list does.

```json
{ "type": "game_rule", "rule": "doWeatherCycle", "value": false }
```

`rule` is the rule's name as Minecraft 1.21.1 names it, such as `doWeatherCycle` or `randomTickSpeed`, on every version
of the game Flatgen runs on: Flatgen turns it into the name a later version gives the same rule. A rule a mod adds is
named as that mod names it. `value` is `true`, `false` or a whole number. A rule the game lacks, or a value it cannot
take, is left as it was and logged. Turning the option off again, then pressing Done, puts the rule back to the game's
default.

### `first_start_time`

Sets the clock once, when the world first starts.

```json
{ "type": "first_start_time", "time": 6000 }
```

`time` is the time of day in ticks from sunrise, from 0 to 23999: 6000 is noon, 13000 nightfall, 18000 midnight.

### `structures`

Adds structure sets to the world's list of structure sets.

```json
{ "type": "structures", "sets": ["skyliners:airports"] }
```

A preset that takes what its biome allows already has every structure set, so this only changes a preset with a chosen
list. A structure still only generates in the biomes it allows.

### `place_at_spawn`

Places a structure template once, when the world first starts, standing on the top layer at the spawn point.

```json
{
  "type": "place_at_spawn",
  "template": "skyliners:airport",
  "align": "center",
  "offset": [0, -1, 0],
  "rotation": "none",
  "void_height": 64,
  "spawn": [20, 1, 8]
}
```

| Key | Default | What it is |
|---|---|---|
| `template` | required | The template's id, `data/<namespace>/structure/<path>.nbt`. |
| `align` | `center` | `center` centres the template's footprint on the spawn's column; `corner` puts its origin there. |
| `offset` | `[0, 0, 0]` | Moves the template after aligning it, in blocks. Its floor stands on the first block above the top layer, so a `y` of `-1` sinks the floor into the top layer. |
| `rotation` | `none` | `none`, `clockwise_90`, `180` or `counterclockwise_90`, turning the template about its origin. |
| `void_height` | `64` | The Y the floor stands at in a world with no layers. |
| `spawn` | none | A block of the template, counted from its origin before turning, that the world's spawn moves to, so players land on the runway rather than the roof. Give a block with open sky above. |

Blocks and entities of the template are placed. A template the game lacks is logged and left out; the world still
opens.

## When two options disagree

Two options that set the same game rule or the clock to different values cannot be on together: turning one on turns
the other off. Options that set them to the same value can be on together, structure sets add up, and templates are all
placed, in id order, so give them offsets to keep them apart.

## Presets

A preset names the options that are set otherwise than their default:

```json
"options": { "flatgen:always_day": true, "skyliners:airport": false }
```

So a mod that adds an option changes no existing preset. An option the preset names that is not installed stays in the
preset as it is, shows as a locked switch in the editor, and is noted in the log when Done is pressed.

## Worlds stay vanilla

Game rules and structure lists are the game's own settings, and first-start effects leave only blocks and the clock
behind, so a world made with options opens without Flatgen or the mod. First-start effects run only on the world
created from the Create World screen whose Done applied them, once: never on a world created after Cancel, nor on one
opened again.

## Examples

Flatgen's own "always day" (F10):

```json
{
  "icon": "minecraft:clock",
  "group": "flatgen:time_and_weather",
  "effects": [
    { "type": "game_rule", "rule": "doDaylightCycle", "value": false },
    { "type": "first_start_time", "time": 6000 }
  ]
}
```

An airport at the spawn, from a mod such as Skyliners, which ships the file and the template in its own jar:

```json
{
  "icon": { "texture": "skyliners:textures/gui/flatgen/airport.png" },
  "group": "skyliners",
  "default": true,
  "requires": ["skyliners"],
  "effects": [
    {
      "type": "place_at_spawn",
      "template": "skyliners:airport",
      "offset": [0, -1, 0],
      "spawn": [20, 1, 8]
    },
    { "type": "game_rule", "rule": "spawnRadius", "value": 0 }
  ]
}
```

## Trying an option

Open Create World, choose Superflat, press Customize: the option shows under its group. If it does not, the game's log
(`logs/latest.log`) has a line naming the option: what is wrong with its file, or that Flatgen hides it because a
required mod is missing.
