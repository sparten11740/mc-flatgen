# F26 Blockpilot commands

Milestone: M1 layer editor · Release: v1 · Status: todo · Depends on: F01

## Why

From F03 on, Flatgen's editor is made of widgets it draws itself (the column, the pickers), which Blockpilot's `screen`
lists as one opaque widget at most. Play tests need to know where each layer, block and card is to click and drag it as
a player would, and what the editor holds to check the result. Blockpilot lets mods add commands of their own
(`docs/extending.md` in Blockpilot) for exactly this.

## What the player gets

Nothing without Blockpilot. With it, an agent can read and drive Flatgen's editor, which is also how Flatgen's own play
tests run.

## Acceptance criteria

- [ ] `flatgen:editor` (query): whether Flatgen's editor is open, and its settings as fields: the layers bottom up
  (`block*height` each, the vanilla code's form), the biome, the selected layer, the surface Y.
- [ ] `flatgen:open` (action): from the title screen or the world list, opens Create World with Superflat chosen and
  presses Customize, so later play tests start in the editor in one step (F01's tests keep driving those screens
  themselves).
- [ ] Each later feature that draws its own widgets adds a query listing them with keys and bounds, as `screen` does
  (`flatgen:column` with F03: each layer's key, block, height, Y range and bounds), so `click` and `drag` can target
  them.
- [ ] Commands are registered only when Blockpilot is installed; without it nothing of theirs loads, and the jar works
  as before.
- [ ] Compiled against `blockpilot-api` from Blockpilot's GitHub release (the API jar attached to `v1.4.0`), so the
  build needs no local Maven repository.
- [ ] Both loaders' metadata name Blockpilot as optional, from the release the commands are tested with.

## Design

- `compat/blockpilot/`: one class per command, listed in `compat/blockpilot/BlockpilotCommands`, which registers them
  under the namespace `flatgen`. Adding a command is a class and a list entry.
- Commands read the editor through what its widgets offer (their layout and the edited preset), never through
  reflection; a widget that a command needs to read gains a small public method for it.
- Each loader's entry point registers the commands when Blockpilot is loaded (`ModList` on NeoForge, `FabricLoader` on
  Fabric, which gets a client entry point again for this). The dependency rules gain a row: `compat/<mod>/` may depend
  on anything but the platform halves, and only the platform halves depend on it (`docs/plan.md` 3.1, enforced by the
  boundaries test).
- Blockpilot's safety setting `all`, its default, allows the commands; a player who sets `play` must list `flatgen` in
  `allowedExtensions`.

## Version notes

- `blockpilot-api` is the same jar for every node; Blockpilot builds for every version Flatgen targets.

## Tests

- Unit: each command's field formatting from a plain preset.
- Dev client on both nodes: `flatgen:open` reaches the editor; `flatgen:editor` reports the classic flat layers; after
  Done and reopening, it reports the same.
