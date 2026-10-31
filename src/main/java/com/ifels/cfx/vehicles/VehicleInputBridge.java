package com.ifels.cfx.vehicles;

import com.ifels.controlflex.api.ControlFlexApi;
import com.ifels.controlflex.api.IControllerState;
import com.ifels.controlflex.api.IInputProvider;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 左摇杆 → 方向键的桥。
 *
 * <p>每 tick 算一次"当前应该按住哪几个键"，存成不可变集合整体替换（{@code volatile}），
 * 供 {@code InterfaceInputMixin} 在任意线程读取时看到一致快照。</p>
 *
 * <p>键码取玩家自己的移动绑定（{@code mc.options.keyUp} 等），所以把 WASD 改成别的布局也能跟上；
 * 但若第三方模组自己有独立键位（IV 的 {@code mtsconfigclient.json}），本桥不认识它们——
 * 这是已知局限，见 README。</p>
 */
public final class VehicleInputBridge {

    private static final Logger LOGGER = LogManager.getLogger("cfx-compat-vehicles");

    private static volatile Set<Integer> held = Set.of();
    private static String lastLog = "";

    private VehicleInputBridge() {
    }

    /** {@code InterfaceInputMixin} 的查询入口。 */
    public static boolean isDown(int keyCode) {
        return held.contains(keyCode);
    }

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null || mc.screen != null) {
            release("no-player-or-screen");
            return;
        }
        Entity ridden = mc.player.getVehicle();
        VehicleTargets.Target target = VehicleTargets.match(ridden);
        if (target == null) {
            release("not-riding");
            return;
        }
        IControllerState state = controllerState();
        if (state == null) {
            release("no-controller");
            return;
        }

        // SDL 约定：Y 轴向下为正，所以"前"是 -Y
        Set<Integer> keys = StickKeys.compute(state.getLeftStickX(), -state.getLeftStickY(),
                keyCode(mc.options.keyUp), keyCode(mc.options.keyDown),
                keyCode(mc.options.keyLeft), keyCode(mc.options.keyRight), StickKeys.THRESHOLD);
        held = keys;
        logIfChanged(target, keys);
    }

    private static IControllerState controllerState() {
        if (!ControlFlexApi.isAvailable()) {
            return null;
        }
        IInputProvider provider = ControlFlexApi.getInputProvider();
        return provider == null ? null : provider.getControllerState();
    }

    /** 非键盘键（例如玩家把前进改成鼠标键）返回 0，该方向不参与。 */
    private static int keyCode(KeyMapping mapping) {
        if (mapping == null) {
            return 0;
        }
        InputConstants.Key key = mapping.getKey();
        if (key == null || key.getType() != InputConstants.Type.KEYSYM) {
            return 0;
        }
        return key.getValue();
    }

    private static void release(String reason) {
        if (!held.isEmpty()) {
            held = Set.of();
        }
        logIfChanged(null, reason);
    }

    private static void logIfChanged(VehicleTargets.Target target, Object detail) {
        String line = target == null
                ? "[cfx-compat-vehicles] idle (" + detail + ")"
                : "[cfx-compat-vehicles] active " + target.label() + " keys=" + detail;
        if (line.equals(lastLog)) {
            return;
        }
        lastLog = line;
        LOGGER.info(line);
    }
}
