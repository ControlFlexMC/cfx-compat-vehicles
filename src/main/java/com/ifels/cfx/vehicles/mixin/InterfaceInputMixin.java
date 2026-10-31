package com.ifels.cfx.vehicles.mixin;

import com.ifels.cfx.vehicles.VehicleInputBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Immersive Vehicles 的按键入口。
 *
 * <p>IV 的 {@code ControlSystem$ControlsKeyboard} 最终走
 * {@code InterfaceInput.isKeyPressed(code)} → {@code GLFW.glfwGetKey}，问的是操作系统，
 * 不读 MC 的 {@code KeyMapping} 也不读 {@code InputConstants}——所以 ControlFlex 现有的
 * keyMapping / virtualKbm / glfwPoll 三条通道都到不了它。唯一可行的位置就是这里：
 * 在它问 GLFW 之后补一句"ControlFlex 的摇杆正按着这个键"。</p>
 *
 * <p>目标是第三方模组类，编译期不在 classpath 上，所以用字符串 targets；
 * {@code require = 0} 让 IV 缺席或换包名（1.21.1 的 {@code mcinterface1211}）时静默不参与。</p>
 */
@Mixin(targets = "mcinterface1201.InterfaceInput", remap = false)
public class InterfaceInputMixin {

    @Inject(method = "isKeyPressed", at = @At("RETURN"), cancellable = true, remap = false)
    private void cfx$vehicleKeys(int keyCode, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && VehicleInputBridge.isDown(keyCode)) {
            cir.setReturnValue(true);
        }
    }
}
