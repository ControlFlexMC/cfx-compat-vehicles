package com.ifels.cfx.vehicles;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StickKeysTest {

    private static final int UP = 87;
    private static final int DOWN = 83;
    private static final int LEFT = 65;
    private static final int RIGHT = 68;
    private static final float T = StickKeys.THRESHOLD;

    private static Set<Integer> keys(float left, float forward) {
        return StickKeys.compute(left, forward, UP, DOWN, LEFT, RIGHT, T);
    }

    @Test
    void forwardAndBack() {
        assertEquals(Set.of(UP), keys(0f, 1f));
        assertEquals(Set.of(DOWN), keys(0f, -1f));
    }

    @Test
    void leftIsPositive() {
        assertEquals(Set.of(LEFT), keys(1f, 0f));
        assertEquals(Set.of(RIGHT), keys(-1f, 0f));
    }

    @Test
    void diagonalPressesBoth() {
        assertEquals(Set.of(UP, LEFT), keys(1f, 1f));
    }

    @Test
    void belowThresholdPressesNothing() {
        assertTrue(keys(0.2f, 0.2f).isEmpty());
        assertTrue(keys(0f, 0f).isEmpty());
    }

    @Test
    void unboundDirectionIsSkipped() {
        assertTrue(StickKeys.compute(0f, 1f, 0, DOWN, LEFT, RIGHT, T).isEmpty(),
                "0 = 该方向没有键盘键码，不应按下任何键");
    }
}
