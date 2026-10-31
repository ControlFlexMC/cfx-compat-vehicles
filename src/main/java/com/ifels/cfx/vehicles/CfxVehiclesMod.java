package com.ifels.cfx.vehicles;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

/**
 * 载具兼容桥入口。
 *
 * <p>每客户端 tick 把左摇杆翻译成"哪几个方向键正被按住"，再由
 * {@code InterfaceInputMixin} 在模组的按键查询处补上。CFX 核心不参与，
 * 对第三方模组的依赖全部关在这个 mod 里。</p>
 */
@Mod(CfxVehiclesMod.MOD_ID)
public class CfxVehiclesMod {

    public static final String MOD_ID = "cfx_compat_vehicles";

    public CfxVehiclesMod() {
        if (FMLEnvironment.dist != Dist.CLIENT) {
            return;
        }
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && Minecraft.getInstance() != null) {
            VehicleInputBridge.tick();
        }
    }
}
