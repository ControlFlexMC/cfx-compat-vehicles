package com.ifels.cfx.vehicles;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 载具兼容桥入口（Minecraft 1.12.2 / Forge）。
 *
 * <p>逻辑全在 {@code InterfaceInputMixin} 里按需计算，没有 tick 也没有常驻状态：
 * 只有载具模组真的去问"这个键按下了吗"的时候才算一次。</p>
 *
 * <p>对第三方模组不做硬依赖：没装就只是什么都不发生。启动时打一行日志说明当前处于哪种状态，
 * 否则"装了但没生效"和"没装"在日志上看不出区别。</p>
 */
@SideOnly(Side.CLIENT)
@Mod(
        modid = CfxVehiclesMod.MOD_ID,
        name = "Control Flex x Vehicles",
        // 与 mcmod.info / gradle.properties 的 mod_version 保持一致（1.12.2 的注解只收字面量）
        version = "0.8.8",
        clientSideOnly = true,
        acceptableRemoteVersions = "*",
        dependencies = "required-after:controlflex"
)
public class CfxVehiclesMod {

    public static final String MOD_ID = "cfx_compat_vehicles";

    private static final Logger LOGGER = LogManager.getLogger("cfx-compat-vehicles");
    private static final String[] SUPPORTED_MODS = {"mts"};

    public CfxVehiclesMod() {
        boolean anyPresent = false;
        for (String modId : SUPPORTED_MODS) {
            if (Loader.isModLoaded(modId)) {
                LOGGER.info("[cfx-compat-vehicles] {} detected — bridge active", modId);
                anyPresent = true;
            }
        }
        if (!anyPresent) {
            LOGGER.info("[cfx-compat-vehicles] No supported vehicle mod installed — bridge inactive");
        }
    }
}
