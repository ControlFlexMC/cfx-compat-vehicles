# cfx-compat-vehicles

Vehicle controller bridge for [ControlFlex](https://www.curseforge.com/minecraft/mc-mods/control-flex) — lets the controller
drive vehicles from mods that **poll raw GLFW key state** instead of reading Minecraft's input.

Currently supports **Immersive Vehicles (MTS)** on **Minecraft 26.1.2 / NeoForge**.

| Branch | Minecraft | Loader |
|---|---|---|
| `1.20.1` | 1.20.1 | Forge |
| `1.21.1` | 1.21.1 | NeoForge |
| `26.1.2` | 26.1.2 | NeoForge |

Immersive Vehicles ships no Fabric build, so every branch is single-loader.

[中文文档](README_ZH.md)

## Why a separate mod

Immersive Vehicles reads its controls through `GLFW.glfwGetKey(window, keyCode)` — it never looks at
`KeyMapping` or `InputConstants`, so ControlFlex's `keyMapping` / `virtualKbm` / `glfwPoll` channels
cannot reach it. The GLFW layer itself is not injectable on NeoForge (library classes are loaded by the
system class loader and never pass through ModLauncher's transformer), so the bridge has to sit inside
the vehicle mod's own read path.

Keeping that mixin out of ControlFlex core means the core stays free of third-party dependencies, and a
failure here is contained to this mod instead of silently affecting every player.

## How it works

* `InterfaceInputMixin` hooks `mcinterface261.InterfaceInput#isKeyPressed` and answers `true` for the
  four movement keys when ControlFlex says the player is steering.
* The signal is the player's own tick-level `ClientInput#getMoveVector()` — the same value vanilla
  movement uses, after ControlFlex's analog policy and response curve. "The player is moving" and
  "the vehicle sees a key" can therefore never disagree.
* When the move vector is zero, the bridge falls back to ControlFlex's action state (`move_forward` …), so it
  keeps working with analog movement turned off — where the stick drives those digital actions instead.
* Nothing is polled on a timer and no global key state is written: the answer is computed only when the
  vehicle actually asks, and a physically pressed key always wins.
* No compat JSON is shipped: the bridge reads ControlFlex's own signals, so there is no rule set to keep in
  sync with the core mod's config format.

## Requirements

| | |
|---|---|
| Minecraft | 26.1.2 |
| Loader | NeoForge 26.1.2+ |
| ControlFlex | 0.8.8+ |
| Immersive Vehicles | 25.0.0 (the 26.1 build) — **optional**, the mod is inert without it |

## Known limitations

* **Immersive Vehicles' own key bindings are ignored.** The bridge uses Minecraft's movement keys. If you
  remapped `car.gas` / `car.turn_*` inside `config/mtsconfigclient.json`, the controller will send the
  wrong codes — reset IV's keyboard controls to `W/A/S/D` or leave them untouched.
* **Do not bind IV's built-in joystick.** If any IV control is assigned a joystick axis/button, IV sets
  `kbOverride` and ignores the keyboard entirely, which also disables this bridge.
* The mixin targets a version-stamped package (`mcinterface261` on this branch). Another Minecraft
  version needs its own branch.

## Building

```bash
./gradlew build          # jar in build/libs/
tools/build-neoforge.sh  # build + deploy to the configured instance
```
