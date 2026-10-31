package com.ifels.cfx.vehicles;

import com.ifels.controlflex.api.ControlFlexApi;
import com.ifels.controlflex.api.IControlFlexPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * ControlFlex SPI 入口。
 *
 * <p>本桥是<b>被动</b>的：不安装 compat JSON、不推送玩家状态，全部逻辑都在
 * {@code InterfaceInputMixin} 里按需计算。保留这个插件只为对齐同族仓库的注册方式，
 * 并把"API 版本不足"这类信息记进日志。</p>
 */
public class CfxVehiclesPlugin implements IControlFlexPlugin {

    private static final Logger LOGGER = LogManager.getLogger("cfx-compat-vehicles");

    @Override
    public String getModId() {
        return CfxVehiclesMod.MOD_ID;
    }

    @Override
    public void onControlFlexReady() {
        if (!ControlFlexApi.isAvailable()) {
            LOGGER.warn("[cfx-compat-vehicles] ControlFlex API not available — vehicle bridge disabled");
        }
    }
}
