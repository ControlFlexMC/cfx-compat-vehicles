> [English](changelog.md)

## 0.8.8 — 首个版本

用手柄驾驶模组里的载具 —— 首个适配是 Immersive Vehicles。

### 修复

*   Immersive Vehicles 的操控走 `GLFW.glfwGetKey(window, keyCode)`：它问的是操作系统，既不读 `KeyMapping` 也不读 `InputConstants`。ControlFlex 的 `keyMapping` / `virtualKbm` / `glfwPoll` 三条通道都在这里止步，而 GLFW 这一层在 Forge 上不可注入 —— 库类由系统类加载器加载，不经过 ModLauncher 的变换器。于是**键盘能开车，手柄不能**。本模组改在 IV 自己的读键路径上作答：`mcinterface1201.InterfaceInput#isKeyPressed` 在玩家转向时对四个移动键返回按下。

### 新增

*   **驾驶** —— 前进、刹车 / 倒车、左转、右转，走载具本来就在读的四个移动键（`car.gas`、`car.brake`、`car.turn_l`、`car.turn_r`）。
*   **模拟量感知** —— 信号源是玩家 tick 级的 `input.forwardImpulse` / `leftImpulse`，与原生移动用的是同一个值，且已过 ControlFlex 的模拟策略与响应曲线。阈值 0.3 高于 0.15 的摇杆死区，所以死区、曲线与层设置都是你走路时已经调好的那一套。
*   **关掉「模拟移动」也能用** —— 脉冲未判为按下时，回落到 ControlFlex 的 `move_forward` …… 动作状态，此时摇杆走的是这四个数字动作。
*   **键盘优先** —— 物理按下的键先返回，桥不参与；一次按下只触发一次。
*   **不轮询，不注入** —— 没有 tick、没有逐帧扫描，只在载具真的发问时算一次。既不写 GLFW 也不碰 `KeyMapping` —— 桥回答的是查询，不是伪造输入。
*   **不用新绑定** —— 复用 ControlFlex 现有的移动通道：摇杆、四个 `move_*` 动作，或两者都行。也没有 compat JSON —— 桥直接读 ControlFlex 自己的信号，不存在需要跟随核心模组配置格式维护的规则集。
*   **按类别设计，不是单模组补丁** —— 载具控制核心（「前进」的定义、阈值、模拟 → 数字、动作回落、键盘优先）加一层很薄的适配器，IV 只占一个注入方法。第二个载具模组接上自己的读键路径就能继承全部规则 —— 所以 id 是 `cfx_compat_vehicles`，不是 `cfx_compat_mts`。
*   **可选依赖，且仅客户端** —— 没装受支持的载具模组时照常加载、只打一行 `bridge inactive` 日志，什么都不做。没有服务端组件、没有数据包，也不修改 ControlFlex 与 Immersive Vehicles。

### 注意

*   载具端是数字的：告诉 IV 的只有「按下 / 未按下」。模拟感来自阈值被越过的时机，而不是半轴。
*   **IV 自己的键位会被忽略** —— 桥比的是 Minecraft 的移动键码。如果你在 `config/mtsconfigclient.json` 里改过 `car.gas` / `car.turn_*`，请恢复成 `W/A/S/D`。
*   **不要给 IV 绑定它自带的摇杆** —— 只要有任何 IV 控件被指派到手柄轴 / 按键，IV 就会置 `kbOverride` 并完全不再读键盘，桥无从作答。
*   其他载具模组尚未接入 —— 适配层就是它们插进来的地方。
*   不支持：把模拟轴传给载具、载具菜单与 HUD、服务端使用。
*   车不动时先看 `latest.log`：出现 `delivering <slot> as pressed` 说明桥已经送出按下（问题在 IV 那边）；一直不出现说明 IV 根本没问 —— 通常是 `kbOverride`，或 IV 内部改过键位。

需要 **Control Flex 0.8.8+**（编译于 API 0.9.2）与 **Immersive Vehicles 24.x** —— 后者可选，未安装时本模组不产生任何行为。Minecraft 1.20.1，Forge 47+，仅客户端。本 JAR 针对 IV 带版本戳的 `mcinterface1201` 包：换 Minecraft 版本需要单独构建。
