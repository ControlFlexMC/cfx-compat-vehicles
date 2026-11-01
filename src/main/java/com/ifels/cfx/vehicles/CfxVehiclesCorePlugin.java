package com.ifels.cfx.vehicles;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

import java.util.Map;

/**
 * Forge 1.12.2 的 mixin 引导。
 *
 * <p>这个版本的 Forge 不会自己读取 jar 里的 {@code MixinConfigs} 清单属性——jar 必须声明
 * {@code FMLCorePlugin}，由核心插件调 {@link MixinBootstrap#init()} 并把配置注册进
 * {@link Mixins}，否则整包 mixin 一条都不会加载。</p>
 */
@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.Name("cfx-compat-vehicles-core")
public class CfxVehiclesCorePlugin implements IFMLLoadingPlugin {

    public CfxVehiclesCorePlugin() {
        MixinBootstrap.init();
        Mixins.addConfiguration("cfx-compat-vehicles.mixins.json");
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
