# Control Flex × Vehicles

A **vehicle input bridge** for ControlFlex — controller driving for vehicle mods that read raw input.
Built for the category — Immersive Vehicles is the first adapter.

---

## What This Does

Without this mod, a controller **cannot drive these vehicles at all**.

Vehicle mods do not drive like a player, so they do not read input like a player. Immersive Vehicles asks
`GLFW.glfwGetKey(window, keyCode)` directly and never looks at `KeyMapping` or `InputConstants`.
ControlFlex's `keyMapping` / `virtualKbm` / `glfwPoll` channels all stop at that boundary — and GLFW itself
cannot be injected on Forge, because the library classes are loaded by the system class loader and never
pass through ModLauncher's transformers.

The vehicle sees a keyboard that isn't there. The controller sees a vehicle it cannot reach.

This bridge sits **inside the vehicle's read path**. When the vehicle asks "is W held?", it answers from
the player's own movement signal — `input.forwardImpulse` / `leftImpulse`, the same tick-level value
vanilla movement uses, after ControlFlex's analog policy and response curve. When those are zero (analog
movement off), it falls back to ControlFlex's `move_forward` … action state.

**The result:** the stick steers, the vehicle receives exactly the digital keys it expects, and "the player
is moving" and "the vehicle sees the key" can never disagree.

---

## A Bridge for Vehicles, Not a Patch for One Mod

The failure above is not an Immersive Vehicles quirk — it is the shape of the **category**. Driving is a
control scheme, not a feature of one mod: vehicle mods implement it as their own input layer — raw key
polling, their own key configs, their own input screens. A per-mod patch would repeat the same translation
for every vehicle, and each copy would drift.

So the bridge is split in two:

| Layer | Responsibility |
|---|---|
| **Vehicle control core** | What "forward" means, the threshold, analog → digital, action fallback, keyboard priority — once, shared by every adapter. |
| **Vehicle adapter** | The one place a vehicle asks for a key. For Immersive Vehicles that is `InterfaceInput#isKeyPressed`; for another vehicle it is that vehicle's own read path. Thin, and the only per-mod code. |

That split is in the mod's identity, not just its docs: the id is `cfx_compat_vehicles`, not
`cfx_compat_mts`. The namespace is the category.

Today the adapter exists for **Immersive Vehicles (MTS)**. That is the first integration, not the shape of
the mod: a second vehicle mod adds a read-path hook to the same layer and inherits every rule above
unchanged. The core never polls, never stores state, and does nothing until a vehicle asks.

If no supported vehicle mod is installed, the bridge is inert.

Keeping the adapter outside ControlFlex core also keeps the core free of third-party dependencies: if a
vehicle integration breaks, it takes this mod down — not every player's controller.

---

## Features

- 🎮 **Drive with the pad** — forward, brake/reverse, left turn, right turn, delivered on the movement keys
  the vehicle already reads.
- 🕹️ **Analog-aware** — reads the tick-level impulse after ControlFlex's curve, with a 0.3 threshold above
  the 0.15 stick deadzone; the deadzone, curves and layers are the ones you already tuned for walking.
- 🧠 **One source of truth** — the bridge and vanilla movement read the same impulses, so the controller
  and the vehicle can never disagree about whether the player is moving.
- ⌨️ **Keyboard still wins** — a physically held key returns before the bridge is consulted; it only adds
  presses the keyboard did not provide.
- ⏱️ **No polling** — no tick handler, no per-frame scan; the answer is computed only when the vehicle
  actually asks.
- 🔌 **No key injection** — nothing is written to GLFW or `KeyMapping`; the bridge answers a query, it does
  not fake an input.
- 🎛️ **No new bindings** — it uses ControlFlex's existing movement path, so the analog stick or the four
  `move_*` actions you already use on foot drive the vehicle too.
- 🧩 **Optional vehicle dependency** — with no supported vehicle mod the mod loads, logs, and does nothing.
- 🛡️ **Client-side only** — no server component, no packets.
- 🪶 **No compat JSON** — the bridge reads ControlFlex's signals directly, so there is no rule set to keep
  in sync with the core mod's config format.

---

## Dependencies (Required)

| Minecraft | Loader | [ControlFlex](https://www.curseforge.com/minecraft/mc-mods/control-flex) | Immersive Vehicles |
|:---:|:---:|:---:|:---:|
| 1.20.1 | Forge 47+ | ≥ 0.8.8 | 24.x — **optional**, the mod is inert without it |

Built against ControlFlex API 0.9.2. This is a **client-side** mod: ControlFlex is required, Immersive
Vehicles is optional at load time and required for anything to happen.

---

## The Four Slots

| Slot | Vehicle action (IV) | ControlFlex action |
|---|---|---|
| Forward | `car.gas` | `move_forward` |
| Brake / reverse | `car.brake` | `move_backward` |
| Left | `car.turn_l` | `move_left` |
| Right | `car.turn_r` | `move_right` |

Normally the bridge reads the analog impulse; the `move_*` action is the fallback when analog movement is
turned off. Either way there is nothing new to learn — the movement controls you already use on foot drive
the vehicle.

---

## How It Works

```
controller stick / trigger
            ↓
ControlFlex analog policy + response curve
            ↓
player.input.forwardImpulse / leftImpulse     ← the same value vanilla movement uses
            ↓
vehicle asks: isKeyPressed(W)                  ← mcinterface1201.InterfaceInput
            ↓
cfx-compat-vehicles: held if the impulse / action says so
            ↓
the vehicle drives, exactly as if W were held
```

Two details do most of the work:

- **Why the read path.** GLFW cannot be patched on Forge, and ControlFlex writes key state rather than
  firing key events, so no upstream channel reaches a vehicle that polls the OS. The vehicle's own input
  call is the only place that has both the question and the player.
- **Why there is no "is the player driving?" check.** `isKeyPressed` is only called by the vehicle's control
  system; being called is the proof. No reflection into the vehicle, no driving-state tracking.

---

## Compatibility

- ✅ Works in singleplayer and multiplayer — client-side only, nothing is needed on the server.
- ✅ Works alongside the keyboard. A physical press is returned untouched, so one press never fires twice.
- ✅ Follows your current WASD bindings, including remapped layouts.
- ✅ Does not modify ControlFlex or Immersive Vehicles — one mixin, scoped to IV's input interface.
- 📌 Digital at the vehicle end: the vehicle is told "held / not held". The analog feel comes from when the
  threshold is crossed, not from a partial axis.

---

## Known Limitations

- **Immersive Vehicles' own key bindings are ignored.** The bridge compares Minecraft's movement key codes.
  If `car.gas` / `car.turn_*` were remapped in `config/mtsconfigclient.json`, reset them to `W/A/S/D`.
- **Do not bind IV's built-in joystick.** If any IV control is assigned a joystick axis or button, IV sets
  `kbOverride` and stops reading the keyboard entirely — the bridge has nothing to answer.
- **The mixin targets a version-stamped IV package** (`mcinterface1201`). Another Minecraft version needs
  its own build; this JAR is 1.20.1 / Forge.
- **Other vehicle mods are not wired yet.** The adapter layer is where they plug in; today only Immersive
  Vehicles is supported.

---

## Not Supported

- **Analog axes to the vehicle** — the vehicle API asks a boolean per key; the bridge translates intent into
  that model, it does not replace it.
- **Vehicle menus and HUD** — only the four movement keys are answered.
- **Server-side use** — there is no server component.

---

## Notes

- Client-side only; install the 1.20.1 Forge JAR.
- Startup logs say whether an adapter found its vehicle: `bridge active` / `bridge inactive`.
- If the vehicle does not move, check `latest.log` for `delivering <slot> as pressed`. If it never appears,
  IV is not asking — usually `kbOverride`, or keys remapped inside IV.
