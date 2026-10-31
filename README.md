# cfx-compat-vehicles

Vehicle controller bridge for [ControlFlex](https://www.curseforge.com/minecraft/mc-mods/control-flex) — lets the controller
drive vehicles from mods that **poll raw GLFW key state** instead of reading Minecraft's input.

Currently supports **Immersive Vehicles (MTS)**.

## Why a separate mod

Immersive Vehicles reads its controls through `GLFW.glfwGetKey(window, keyCode)` — it never looks at
`KeyMapping` or `InputConstants`, so ControlFlex's `keyMapping` / `virtualKbm` / `glfwPoll` channels
cannot reach it. The GLFW layer itself is not injectable on Forge (library classes are loaded by the
system class loader and never pass through ModLauncher's transformer), so the bridge has to sit inside
the vehicle mod's own read path.

Keeping that mixin out of ControlFlex core means the core stays free of third-party dependencies, and a
failure here is contained to this mod instead of silently affecting every player.

## How it works

* Every client tick, the left stick is translated into "which movement keys are currently held", using
  the player's own `W/A/S/D` bindings (`mc.options.keyUp` …), so rebinding the movement keys keeps working.
* `InterfaceInputMixin` hooks `mcinterface1201.InterfaceInput#isKeyPressed` and answers `true` for those
  keys when the vehicle mod's own check said no.
* Only applies while the player rides a supported vehicle (`mcinterface*.BuilderEntityLinkedSeat`) — see
  `VehicleTargets`.
* The stick values come from the ControlFlex API (`ControlFlexApi.getInputProvider()`), not from a private
  channel.
* The bundled compat JSON (`assets/cfx_compat_vehicles/compat/mts.json`) is installed into
  `config/controlflex/compat/mods/` on startup, gated by SHA-1 so player edits are never overwritten.

## Requirements

| | |
|---|---|
| Minecraft | 1.20.1 |
| Loader | Forge 47+ |
| ControlFlex | 0.8.9+ |
| Immersive Vehicles | 24.x |

## Known limitations

* **Immersive Vehicles' own key bindings are ignored.** The bridge uses Minecraft's movement keys. If you
  remapped `car.gas` / `car.turn_*` inside `config/mtsconfigclient.json`, the controller will send the
  wrong codes — reset IV's keyboard controls to `W/A/S/D` or leave them untouched.
* **Do not bind IV's built-in joystick.** If any IV control is assigned a joystick axis/button, IV sets
  `kbOverride` and ignores the keyboard entirely, which also disables this bridge.
* Movement must be bound to stick axes (or the four stick direction buttons). A D-pad-only binding produces
  no direction vector.
* The mixin targets a version-stamped package (`mcinterface1201`). Another Minecraft version needs its own
  build.

## Building

```bash
./gradlew build          # jar in build/libs/
tools/build-forge.sh     # build + deploy to the configured instance
```
