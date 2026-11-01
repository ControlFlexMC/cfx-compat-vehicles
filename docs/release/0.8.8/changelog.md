> [中文版](changelog.zh.md)

## 0.8.8 — First Release

Drive modded vehicles from the controller — Immersive Vehicles first.

### Fixed

*   Immersive Vehicles reads its controls with `GLFW.glfwGetKey(window, keyCode)`: it asks the operating system, and never looks at `KeyMapping` or `InputConstants`. ControlFlex's `keyMapping` / `virtualKbm` / `glfwPoll` channels all stop at that boundary, and GLFW itself cannot be patched on Forge — its library classes are loaded by the system class loader and never pass through ModLauncher's transformers. So **the keyboard drove the vehicle, the pad did not**. The bridge answers inside IV's own read path instead: `mcinterface1201.InterfaceInput#isKeyPressed` returns held for the four movement keys while the player is steering.

### Added

*   **Drive** — forward, brake/reverse, left turn, right turn, delivered on the movement keys the vehicle already reads (`car.gas`, `car.brake`, `car.turn_l`, `car.turn_r`).
*   **Analog-aware** — the signal is the player's own tick-level `input.forwardImpulse` / `leftImpulse`, the same value vanilla movement uses, after ControlFlex's analog policy and response curve. A 0.3 threshold sits above the 0.15 stick deadzone, so the deadzone, curves and layers are the ones already tuned for walking.
*   **Works with analog movement off** — when the impulse says nothing is held, the bridge falls back to ControlFlex's `move_forward` … action state, where the stick drives those four digital actions instead.
*   **Keyboard still wins** — a physically held key returns before the bridge is consulted; one press never fires twice.
*   **No polling, no injection** — no tick handler and no per-frame scan: the answer is computed only when the vehicle actually asks. Nothing is written to GLFW or `KeyMapping` — the bridge answers a query, it does not fake an input.
*   **Nothing new to bind** — it rides ControlFlex's existing movement path, so the stick, the four `move_*` actions, or both drive the vehicle. No compat JSON either: the bridge reads ControlFlex's own signals, so there is no rule set to keep in sync with the core mod's config format.
*   **Built for the category, not one mod** — a vehicle-control core (what "forward" means, the threshold, analog → digital, action fallback, keyboard priority) plus a thin per-mod adapter, which for IV is a single injected method. A second vehicle mod hooks its read path and inherits every rule unchanged — that is why the id is `cfx_compat_vehicles`, not `cfx_compat_mts`.
*   **Optional, and client-side only** — with no supported vehicle mod installed the mod loads, logs `bridge inactive`, and does nothing. No server component, no packets, and neither ControlFlex nor Immersive Vehicles is modified.

### Notes

*   The vehicle end is digital: IV is told "held / not held". The analog feel comes from when the threshold is crossed, not from a partial axis.
*   **IV's own key bindings are ignored** — the bridge compares Minecraft's movement key codes. If `car.gas` / `car.turn_*` were remapped in `config/mtsconfigclient.json`, reset them to `W/A/S/D`.
*   **Do not bind IV's built-in joystick** — if any IV control is assigned a joystick axis or button, IV sets `kbOverride` and stops reading the keyboard entirely; the bridge has nothing to answer.
*   Other vehicle mods are not wired yet — the adapter layer is where they plug in.
*   Not supported: analog axes to the vehicle, vehicle menus and HUD, server-side use.
*   If the vehicle does not move, check `latest.log`: `delivering <slot> as pressed` means the bridge delivered a press (and the problem is on IV's side); if it never appears, IV is not asking — usually `kbOverride`, or keys remapped inside IV.

Requires **Control Flex 0.8.8+** (built against API 0.9.2) and **Immersive Vehicles 24.x** — optional, nothing happens without it. On Minecraft 1.20.1, Forge 47+. Client-side only. This JAR targets IV's version-stamped `mcinterface1201` package: another Minecraft version needs its own build.
