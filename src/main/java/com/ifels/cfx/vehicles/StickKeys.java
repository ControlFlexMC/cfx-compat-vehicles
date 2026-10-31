package com.ifels.cfx.vehicles;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 摇杆方向 → 键集合的纯逻辑（不碰任何 Minecraft 类型，便于单测）。
 */
final class StickKeys {

    /** 与 ControlFlex 八向量化同源的阈值。 */
    static final float THRESHOLD = 0.3f;

    private StickKeys() {
    }

    /**
     * @param left    正 = 往左推（与 Minecraft {@code Input.leftImpulse} 同向）
     * @param forward 正 = 往前推
     * @param up      前进键码；0 = 该方向不可用（被改成鼠标键等）
     */
    static Set<Integer> compute(float left, float forward,
                                int up, int down, int leftKey, int rightKey, float threshold) {
        Set<Integer> out = new LinkedHashSet<>(4);
        if (forward > threshold) {
            addIfBound(out, up);
        }
        if (forward < -threshold) {
            addIfBound(out, down);
        }
        if (left > threshold) {
            addIfBound(out, leftKey);
        }
        if (left < -threshold) {
            addIfBound(out, rightKey);
        }
        return out;
    }

    private static void addIfBound(Set<Integer> out, int code) {
        if (code != 0) {
            out.add(code);
        }
    }
}
