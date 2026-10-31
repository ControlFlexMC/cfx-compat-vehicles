package com.ifels.cfx.vehicles;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VehicleControlTest {

    @Test
    void forwardAndBackward() {
        assertTrue(VehicleControl.FORWARD.isHeld(1f, 0f));
        assertTrue(VehicleControl.BACKWARD.isHeld(-1f, 0f));
        assertFalse(VehicleControl.FORWARD.isHeld(-1f, 0f));
    }

    @Test
    void leftImpulseIsPositiveToTheLeft() {
        assertTrue(VehicleControl.LEFT.isHeld(0f, 1f));
        assertTrue(VehicleControl.RIGHT.isHeld(0f, -1f));
        assertFalse(VehicleControl.LEFT.isHeld(0f, -1f));
    }

    @Test
    void belowThresholdIsNotHeld() {
        assertFalse(VehicleControl.FORWARD.isHeld(0.2f, 0f));
        assertFalse(VehicleControl.LEFT.isHeld(0f, 0.2f));
    }

    @Test
    void eachSlotHasItsOwnAction() {
        assertEquals("move_forward", VehicleControl.FORWARD.actionId());
        assertEquals("move_backward", VehicleControl.BACKWARD.actionId());
        assertEquals("move_left", VehicleControl.LEFT.actionId());
        assertEquals("move_right", VehicleControl.RIGHT.actionId());
    }
}
