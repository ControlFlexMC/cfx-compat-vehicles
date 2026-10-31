package com.ifels.cfx.vehicles;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

/**
 * 载具兼容桥入口。
 *
 * <p>逻辑全在 {@code InterfaceInputMixin} 里按需计算，没有 tick 也没有常驻状态：
 * 只有 IV 真的去问"这个键按下了吗"的时候才算一次。</p>
 */
@Mod(CfxVehiclesMod.MOD_ID)
public class CfxVehiclesMod {

    public static final String MOD_ID = "cfx_compat_vehicles";

    public CfxVehiclesMod() {
        if (FMLEnvironment.dist != Dist.CLIENT) {
            return;
        }
        // 仅用于生命周期占位；注入点与配置安装都不依赖这里。
    }
}
