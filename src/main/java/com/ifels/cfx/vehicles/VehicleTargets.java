package com.ifels.cfx.vehicles;

import net.minecraft.world.entity.Entity;

import java.util.List;

/**
 * 支持哪些载具。
 *
 * <p>按被骑乘实体的类名前缀/后缀判定，不直接引用第三方类型——加一款新载具时
 * 往 {@link #TARGETS} 里加一条，再在 {@code mixin} 包里加它对应的注入点。</p>
 */
public final class VehicleTargets {

    /**
     * @param classPrefix 类名前缀（第三方模组的适配层包，通常带版本戳）
     * @param classSuffix 类名后缀（实体类，不含版本戳）
     * @param label       日志用的人类可读名字
     */
    public record Target(String classPrefix, String classSuffix, String label) {
    }

    private static final List<Target> TARGETS = List.of(
            // Immersive Vehicles (mts)：玩家坐在 BuilderEntityLinkedSeat 上；
            // 包名随版本变（1.20.1 = mcinterface1201，1.21.1 = mcinterface1211），所以只锁前后缀。
            new Target("mcinterface", ".BuilderEntityLinkedSeat", "Immersive Vehicles")
    );

    private VehicleTargets() {
    }

    /** 命中的目标，未命中返回 null。 */
    public static Target match(Entity ridden) {
        if (ridden == null) {
            return null;
        }
        String name = ridden.getClass().getName();
        for (Target t : TARGETS) {
            if (name.startsWith(t.classPrefix()) && name.endsWith(t.classSuffix())) {
                return t;
            }
        }
        return null;
    }
}
