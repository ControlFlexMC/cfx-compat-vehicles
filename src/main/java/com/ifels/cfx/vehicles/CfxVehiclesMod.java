package com.ifels.cfx.vehicles;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 载具兼容桥入口（Minecraft 1.21.1 / NeoForge）。
 *
 * <p>逻辑全在 {@code InterfaceInputMixin} 里按需计算，没有 tick 也没有常驻状态：
 * 只有载具模组真的去问"这个键按下了吗"的时候才算一次。</p>
 *
 * <p>对第三方模组不做硬依赖：没装就只是什么都不发生。启动时打一行日志说明当前处于哪种状态，
 * 否则"装了但没生效"和"没装"在日志上看不出区别。</p>
 */
@Mod(CfxVehiclesMod.MOD_ID)
public class CfxVehiclesMod {

    public static final String MOD_ID = "cfx_compat_vehicles";

    private static final Logger LOGGER = LogManager.getLogger("cfx-compat-vehicles");
    private static final String[] SUPPORTED_MODS = {"mts"};

    public CfxVehiclesMod() {
        if (FMLEnvironment.dist != Dist.CLIENT) {
            return;
        }
        boolean anyPresent = false;
        for (String modId : SUPPORTED_MODS) {
            if (ModList.get().isLoaded(modId)) {
                LOGGER.info("[cfx-compat-vehicles] {} detected — bridge active", modId);
                anyPresent = true;
            }
        }
        if (!anyPresent) {
            LOGGER.info("[cfx-compat-vehicles] No supported vehicle mod installed — bridge inactive");
        }
    }
}
