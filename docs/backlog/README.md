# Flatgen backlog

One file per feature, worked through from top to bottom. Each file says why the feature exists, what the player gets,
its acceptance criteria, the design, what differs between Minecraft versions, and how it is tested. `docs/plan.md` is
the overall plan (what Flatgen is, its architecture and the rules the code keeps); this folder is the work.

## How to work through it

- Take the first feature whose status is `todo` and whose dependencies are `done`. Set it to `doing`.
- A feature is `done` when every acceptance criterion is ticked, its tests pass on every node of the version it targets
  (`./gradlew build`), and what a player sees has been tried in a dev client of each node (Blockpilot where it can
  reach the screen, by hand otherwise; the feature file says which were not tried).
- Update the status here and in the feature file in the same commit that finishes it.
- A feature found to need splitting gets a new file with the next free number; numbers are never reused.
- Every feature is built for all nodes of its version at once; a later version's port (F23 to F25) brings every feature
  done before it along.

## Versions

| Release | Minecraft | Loaders | Nodes |
|---|---|---|---|
| v1 | 1.21.1 | NeoForge, Fabric | `1.21.1-neoforge`, `1.21.1-fabric` |
| v2 | 26.1 | NeoForge, Fabric | `26.1-neoforge`, `26.1-fabric` |
| v3 | 1.20.1 | Forge, Fabric | `1.20.1-forge`, `1.20.1-fabric` |
| v4 | 1.21.11, 26.2, 26.3 | NeoForge, Fabric | as Blockpilot builds them |

v3 and v4 may swap; both come after v2.

## Milestones

### v1: Minecraft 1.21.1 on NeoForge and Fabric

**M0, foundation**

| ID | Feature | Status | Depends on |
|---|---|---|---|
| F00 | [Project setup](00-project-setup.md) | done | |
| F01 | [Customize opens Flatgen](01-customize-hook.md) | done | F00 |

**M1, layer editor**

| ID | Feature | Status | Depends on |
|---|---|---|---|
| F02 | [Preset model and file format](02-preset-model.md) | done | F00 |
| F26 | [Blockpilot commands](26-blockpilot-commands.md) | done | F01 |
| F03 | [Layer column](03-layer-column.md) | done | F01, F02, F26 |
| F04 | [Layer editing](04-layer-editing.md) | done | F03 |
| F05 | [Block picker](05-block-picker.md) | done | F04 |
| F06 | [Vanilla superflat code](06-vanilla-code.md) | done | F02 |

**M2, biome and world**

| ID | Feature | Status | Depends on |
|---|---|---|---|
| F27 | [World options from other mods](27-world-options.md) | done | F02, F01 |
| F07 | [Biome picker](07-biome-picker.md) | todo | F03 |
| F08 | [World facts](08-world-facts.md) | todo | F07 |
| F09 | [Structures and decorations](09-structures-and-decorations.md) | todo | F03 |
| F10 | [Time, weather and mob options](10-world-rules.md) | todo | F27 |

**M3, presets**

| ID | Feature | Status | Depends on |
|---|---|---|---|
| F11 | [Preset library](11-preset-library.md) | todo | F02, F03 |
| F12 | [Favourites and default](12-favourites-and-default.md) | todo | F11 |
| F13 | [Built-in presets](13-built-in-presets.md) | todo | F11 |
| F14 | [Templates](14-templates.md) | todo | F11 |
| F15 | [Sharing preset files](15-preset-files.md) | todo | F11 |

**M4, finish and release v1**

| ID | Feature | Status | Depends on |
|---|---|---|---|
| F16 | [Works without reading](16-picture-first.md) | todo | F03 to F15 |
| F17 | [Pack author support](17-pack-authors.md) | todo | F12, F13 |
| F18 | [Other flat world types](18-other-flat-world-types.md) | todo | F01 |
| F19 | [English and German](19-translations.md) | todo | F16 |
| F20 | [Release v1](20-release-v1.md) | todo | F00 to F19 |

### After v1, still 1.21.1 first

| ID | Feature | Status | Depends on |
|---|---|---|---|
| F21 | [Exports for servers and datapacks](21-exports.md) | todo | F20 |
| F22 | [Flat Nether and End](22-flat-nether-end.md) | todo | F20 |
| F28 | [Java API for world options](28-options-java-api.md) | todo | F27, F20 |

### Design track, when the owner asks for it

| ID | Feature | Status | Depends on |
|---|---|---|---|
| F29 | [A simpler editor a child can work out](29-simple-editor-prototype.md) | todo | F06 |

### v2 and later

| ID | Feature | Status | Depends on |
|---|---|---|---|
| F23 | [Port to 26.1 (v2)](23-port-26-1.md) | todo | F20 |
| F24 | [Port to 1.20.1 (v3)](24-port-1-20-1.md) | todo | F23 |
| F25 | [Port to 1.21.11, 26.2 and 26.3 (v4)](25-port-1-21-11-26-2-26-3.md) | todo | F23 |
