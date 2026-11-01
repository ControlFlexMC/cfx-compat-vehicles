# cfx-compat-vehicles

ControlFlex ↔ 载具桥接模组 — 让手柄能驾驶那些**直接轮询 GLFW 按键状态**、而不读 Minecraft 输入的模组载具。

目前支持 **Immersive Vehicles (MTS)**，对应 **Minecraft 1.21.1 / NeoForge**。

| 分支 | Minecraft | 加载器 |
|---|---|---|
| `1.20.1` | 1.20.1 | Forge |
| `1.21.1` | 1.21.1 | NeoForge |

Immersive Vehicles 没有 Fabric 构建，因此每个分支都是单加载器。

[English](README.md)

## 为什么需要独立一个模组

Immersive Vehicles 的操控走 `GLFW.glfwGetKey(window, keyCode)` —— 它既不读 `KeyMapping` 也不读
`InputConstants`，所以 ControlFlex 的 `keyMapping` / `virtualKbm` / `glfwPoll` 三条通道都到不了它。
而 GLFW 这一层在 Forge 上不可注入（库类由系统类加载器加载，不经过 ModLauncher 的变换器），
因此桥必须落在载具模组自己的读键路径里。

把这段 mixin 留在 ControlFlex 核心里，意味着核心要背上第三方依赖、且失败会静默影响所有玩家；
放到独立模组后，依赖是显式的，出问题也只影响这一个适配。

## 工作原理

* `InterfaceInputMixin` 注入 `mcinterface1201.InterfaceInput#isKeyPressed`，当 ControlFlex 认为玩家在转向时，
  对四个移动键返回 `true`。
* 信号源是玩家自己的 tick 级 `input.forwardImpulse` / `leftImpulse` —— 与原生移动用的是同一个值，
  且已经过 ControlFlex 的模拟策略与响应曲线。所以"玩家在动"和"载具看到按键"不可能互相矛盾。
* 脉冲为零时回落到 ControlFlex 的动作状态（`move_forward` 等），这样关掉"模拟移动"也能用 ——
  那种情况摇杆走的是这些数字动作。
* 没有任何定时轮询，也不写全局键状态：只在载具真的发问时才算一次，且物理按下的键永远优先。

## 环境要求

| | |
|---|---|
| Minecraft | 1.21.1 |
| 加载器 | NeoForge 21.1+ |
| ControlFlex | 0.8.9+ |
| Immersive Vehicles | 24.x —— **可选**，未安装时本模组不产生任何行为 |

## 已知局限

* **Immersive Vehicles 自己的键位会被忽略。** 本桥用的是 Minecraft 的移动键。如果你在
  `config/mtsconfigclient.json` 里改过 `car.gas` / `car.turn_*`，手柄发出的键码会对不上 ——
  请把 IV 的键盘控制恢复成 `W/A/S/D`，或保持默认不动。
* **不要给 IV 绑定它自带的摇杆。** 只要有任何 IV 控件被指派到手柄轴/按键，IV 就会置 `kbOverride`
  并完全忽略键盘，本桥随之失效。
* mixin 目标是带版本戳的包名（本分支为 `mcinterface1211`）。换 Minecraft 版本需要单独开分支。

## 构建

```bash
./gradlew build          # 产物在 build/libs/
tools/build-neoforge.sh  # 构建并部署到配置好的实例
```
