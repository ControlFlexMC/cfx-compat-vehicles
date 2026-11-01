package com.ifels.cfx.vehicles.mixin;

import com.ifels.cfx.vehicles.VehicleControl;
import com.ifels.controlflex.api.ControlFlexApi;
import com.ifels.controlflex.api.IActionStateProvider;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Immersive Vehicles 的按键入口。
 *
 * <h2>为什么注入这里</h2>
 * IV 的 {@code ControlSystem$ControlsKeyboard} 走
 * {@code InterfaceInput.isKeyPressed(code)} → {@code GLFW.glfwGetKey}，问的是操作系统，
 * 既不读 {@code KeyMapping} 也不读 {@code InputConstants}——ControlFlex 现有的
 * keyMapping / virtualKbm / glfwPoll 三条通道都到不了它。GLFW 层本身在 Forge 上不可注入
 * （库类由系统类加载器加载，不经过 ModLauncher 的变换器），所以只能在这里补一句。
 *
 * <h2>为什么不需要"玩家是否在车上"的判断</h2>
 * {@code isKeyPressed} 只被 IV 的载具控制系统调用，"这个方法被调用"本身已经说明玩家在驾驶，
 * 因此不需要反射进 IV 内部或另行跟踪驾驶状态。
 *
 * <h2>边界</h2>
 * <ul>
 *   <li>物理键已经按下时原样返回，保持键盘优先。</li>
 *   <li>ControlFlex 不可用、玩家或 input 尚未就绪时降级为原值。</li>
 *   <li>只读：不写任何全局键状态，也不碰 {@code KeyMapping}。</li>
 * </ul>
 */
@Mixin(targets = "mcinterface1182.InterfaceInput", remap = false)
public class InterfaceInputMixin {

    @Unique
    private static final org.apache.logging.log4j.Logger CFX$LOGGER =
            org.apache.logging.log4j.LogManager.getLogger("cfx-compat-vehicles");

    @Unique
    private static boolean cfx$reported;

    @Inject(method = "isKeyPressed", at = @At("RETURN"), cancellable = true, remap = false, require = 0)
    private void cfx$vehicleKeys(int keyCode, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() || !ControlFlexApi.isAvailable()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || player.input == null) {
            return;
        }
        VehicleControl control = cfx$resolve(keyCode, mc.options);
        if (control == null) {
            return;
        }

        boolean held = control.isHeld(player.input.forwardImpulse, player.input.leftImpulse);
        if (!held) {
            held = cfx$actionHeld(control.actionId());
        }
        if (held) {
            if (!cfx$reported) {
                // 只报一次：说明注入生效且 IV 真的用这个键码问过。
                // 若这行出现但车仍不动，问题在 IV 侧（例如 kbOverride），不在本桥。
                cfx$reported = true;
                CFX$LOGGER.info("[cfx-compat-vehicles] delivering {} as pressed (keyCode={})",
                        control, keyCode);
            }
            cir.setReturnValue(true);
        }
    }

    /**
     * IV 传进来的是键码，不是 KeyMapping，所以只能比键码。
     *
     * <p>键码取玩家当前的移动绑定，把 WASD 改成别的布局也能跟上；但 IV 自己的
     * {@code mtsconfigclient.json} 若被改过，这里对不上（已知局限，见 README）。</p>
     */
    private static VehicleControl cfx$resolve(int keyCode, Options options) {
        if (options == null || keyCode == 0) {
            return null;
        }
        if (keyCode == cfx$keyCode(options.keyUp)) {
            return VehicleControl.FORWARD;
        }
        if (keyCode == cfx$keyCode(options.keyDown)) {
            return VehicleControl.BACKWARD;
        }
        if (keyCode == cfx$keyCode(options.keyLeft)) {
            return VehicleControl.LEFT;
        }
        if (keyCode == cfx$keyCode(options.keyRight)) {
            return VehicleControl.RIGHT;
        }
        return null;
    }

    /** 非键盘键（被改成鼠标键等）返回 0，该方向不参与。 */
    private static int cfx$keyCode(KeyMapping mapping) {
        if (mapping == null) {
            return 0;
        }
        InputConstants.Key key = mapping.getKey();
        if (key == null || key.getType() != InputConstants.Type.KEYSYM) {
            return 0;
        }
        return key.getValue();
    }

    /** 关掉"模拟移动"后摇杆走动作通道，这里补上。 */
    private static boolean cfx$actionHeld(String actionId) {
        IActionStateProvider actions = ControlFlexApi.getActionStateProvider();
        return actions != null && actions.isGameActionActive(actionId);
    }
}
