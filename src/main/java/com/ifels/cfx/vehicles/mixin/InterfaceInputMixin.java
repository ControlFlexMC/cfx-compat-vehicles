package com.ifels.cfx.vehicles.mixin;

import com.ifels.cfx.vehicles.VehicleControl;
import com.ifels.controlflex.api.ControlFlexApi;
import com.ifels.controlflex.api.IActionStateProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Immersive Vehicles 的按键入口（Minecraft 1.12.2 / Forge）。
 *
 * <h2>为什么注入这里</h2>
 * IV 的 {@code ControlSystem$ControlsKeyboard} 走
 * {@code InterfaceInput.isKeyPressed(code)} → {@code GLFW.glfwGetKey}，问的是操作系统，
 * 既不读 {@code KeyBinding} 也不读 {@code Keyboard}。GLFW 层在 Forge 上不可注入（库类由
 * 系统类加载器加载，不经过 ModLauncher 的变换器），所以只能在这里补一句。
 *
 * <h2>1.12.2 的信号源</h2>
 * 这个版本没有 {@code Input.forwardImpulse}，用的是 {@code EntityPlayerSP.moveForward}
 * 与 {@code moveStrafing}（正数分别为前进、向左），语义与高版本的
 * {@code forwardImpulse}/{@code leftImpulse} 一致。
 *
 * <h2>边界</h2>
 * <ul>
 *   <li>物理键已经按下时原样返回，保持键盘优先。</li>
 *   <li>ControlFlex 不可用、玩家尚未就绪时降级为原值。</li>
 *   <li>只读：不写任何全局键状态。</li>
 * </ul>
 */
@Mixin(targets = "mcinterface1122.InterfaceInput", remap = false)
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
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null || mc.gameSettings == null) {
            return;
        }
        VehicleControl control = cfx$resolve(keyCode, mc.gameSettings);
        if (control == null) {
            return;
        }

        boolean held = control.isHeld(mc.player.moveForward, mc.player.moveStrafing);
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
     * IV 传进来的是键码，不是 KeyBinding，所以只能比键码。
     *
     * <p>键码取玩家当前的移动绑定；IV 自己的 {@code mtsconfigclient.json} 若被改过，这里对不上
     * （已知局限，见 README）。</p>
     */
    private static VehicleControl cfx$resolve(int keyCode, GameSettings settings) {
        if (keyCode <= 0) {
            return null;
        }
        if (keyCode == cfx$keyCode(settings.keyBindForward)) {
            return VehicleControl.FORWARD;
        }
        if (keyCode == cfx$keyCode(settings.keyBindBack)) {
            return VehicleControl.BACKWARD;
        }
        if (keyCode == cfx$keyCode(settings.keyBindLeft)) {
            return VehicleControl.LEFT;
        }
        if (keyCode == cfx$keyCode(settings.keyBindRight)) {
            return VehicleControl.RIGHT;
        }
        return null;
    }

    /** 1.12.2 里绑定到鼠标的键是负值，统一视为"该方向不可用"。 */
    private static int cfx$keyCode(KeyBinding binding) {
        if (binding == null) {
            return 0;
        }
        int code = binding.getKeyCode();
        return code > 0 ? code : 0;
    }

    /** 关掉"模拟移动"后摇杆走动作通道，这里补上。 */
    private static boolean cfx$actionHeld(String actionId) {
        IActionStateProvider actions = ControlFlexApi.getActionStateProvider();
        return actions != null && actions.isGameActionActive(actionId);
    }
}
