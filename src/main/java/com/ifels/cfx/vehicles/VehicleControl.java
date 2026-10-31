package com.ifels.cfx.vehicles;

/**
 * 载具的四个方向槽位。纯逻辑，不依赖 Minecraft 类型，便于单测。
 *
 * <h2>为什么读玩家的 input 脉冲而不是摇杆原值</h2>
 * {@code LocalPlayer.input.forwardImpulse/leftImpulse} 是 tick 级、已经过 ControlFlex
 * 的模拟策略与响应曲线的值，而且<b>和玩家自己移动用的是同一个值</b>——所以"玩家在动"和
 * "载具看到按键"不可能互相矛盾。直接读 API 的 {@code getControllerState()} 会混入渲染帧插值，
 * 与 tick 快照不同步。
 *
 * <h2>为什么还要回落到动作状态</h2>
 * ControlFlex 只在"模拟移动"开启时写脉冲；关掉之后摇杆被路由到 {@code move_*} 数字动作，
 * 脉冲保持零。只读脉冲会让这部分玩家开不动车，所以脉冲为假时再查一次动作状态。
 */
public enum VehicleControl {

    /** 前进 - IV 的 {@code car.gas}。 */
    FORWARD("move_forward"),
    /** 后退 / 刹车 - IV 的 {@code car.brake}。 */
    BACKWARD("move_backward"),
    /** 左转 - IV 的 {@code car.turn_l}。 */
    LEFT("move_left"),
    /** 右转 - IV 的 {@code car.turn_r}。 */
    RIGHT("move_right");

    /** 与 ControlFlex 的 {@code Thresholds.STICK_THRESHOLD} 同值，是 0.15 死区的超集。 */
    public static final float MOVE_THRESHOLD = 0.3f;

    private final String actionId;

    VehicleControl(String actionId) {
        this.actionId = actionId;
    }

    /** 驱动本槽位的 ControlFlex 动作。 */
    public String actionId() {
        return actionId;
    }

    /**
     * @param forwardImpulse 正 = 前进
     * @param leftImpulse    正 = 往左（与 vanilla {@code Input} 同向）
     */
    public boolean isHeld(float forwardImpulse, float leftImpulse) {
        return switch (this) {
            case FORWARD -> forwardImpulse >= MOVE_THRESHOLD;
            case BACKWARD -> forwardImpulse <= -MOVE_THRESHOLD;
            case LEFT -> leftImpulse >= MOVE_THRESHOLD;
            case RIGHT -> leftImpulse <= -MOVE_THRESHOLD;
        };
    }
}
