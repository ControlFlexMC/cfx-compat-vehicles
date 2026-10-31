package com.ifels.cfx.vehicles;

import com.ifels.controlflex.api.ControlFlexApi;
import com.ifels.controlflex.api.ICompatAssetInstaller;
import com.ifels.controlflex.api.IControlFlexPlugin;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * SPI 插件：把 IV 的 compat JSON 装进 {@code config/controlflex/compat/mods/}。
 *
 * <p>规则本身没有变（{@code vehicleInput} + {@code mcinterface*.BuilderEntityLinkedSeat} +
 * {@code GLFW_VIRTUAL_KEY}），只是从"CFX jar 内置"改成"由 compat 模组分发"——
 * 和 {@code cfx-compat-epicfight} 同一条路。</p>
 */
public class CfxVehiclesPlugin implements IControlFlexPlugin {

    private static final Logger LOGGER = LogManager.getLogger("cfx-compat-vehicles");
    private static final String COMPAT_RESOURCE = "/assets/cfx_compat_vehicles/compat/mts.json";

    @Override
    public String getModId() {
        return CfxVehiclesMod.MOD_ID;
    }

    @Override
    public void onInstallCompatConfigs(ICompatAssetInstaller installer) {
        byte[] bundled = readClasspathResource(COMPAT_RESOURCE);
        if (bundled == null) {
            LOGGER.warn("[cfx-compat-vehicles] Bundled compat resource missing: {}", COMPAT_RESOURCE);
            return;
        }

        Path modsDir = resolveModsDir();
        if (modsDir == null) {
            LOGGER.warn("[cfx-compat-vehicles] Game directory unavailable; installing compat without SHA-1 gate");
            install(installer);
        } else if (CompatConfigInstaller.needsOverwrite(bundled, modsDir.resolve(CompatConfigInstaller.COMPAT_FILE_NAME))) {
            install(installer);
        }

        if (!CompatConfigInstaller.deleteLegacyFile(modsDir)) {
            LOGGER.warn("[cfx-compat-vehicles] Failed to delete leftover {}",
                    CompatConfigInstaller.LEGACY_FILE_NAME);
        }
    }

    @Override
    public void onControlFlexReady() {
        if (!ControlFlexApi.isAvailable()) {
            LOGGER.warn("[cfx-compat-vehicles] ControlFlex API not available — vehicle bridge disabled");
        }
    }

    private static void install(ICompatAssetInstaller installer) {
        if (installer.install(COMPAT_RESOURCE, CompatConfigInstaller.COMPAT_FILE_NAME)) {
            LOGGER.info("[cfx-compat-vehicles] Installed {} (SHA-1 differed or file missing)",
                    CompatConfigInstaller.COMPAT_FILE_NAME);
        } else {
            LOGGER.warn("[cfx-compat-vehicles] Failed to install {}", CompatConfigInstaller.COMPAT_FILE_NAME);
        }
    }

    private static Path resolveModsDir() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.gameDirectory != null) {
            return mc.gameDirectory.toPath().resolve(CompatConfigInstaller.MODS_RELATIVE);
        }
        try {
            Path gameDir = FMLPaths.GAMEDIR.get();
            if (gameDir != null) {
                return gameDir.resolve(CompatConfigInstaller.MODS_RELATIVE);
            }
        } catch (RuntimeException e) {
            LOGGER.warn("[cfx-compat-vehicles] FMLPaths.GAMEDIR unavailable: {}", e.getMessage());
        }
        return null;
    }

    private static byte[] readClasspathResource(String path) {
        try (InputStream in = CfxVehiclesPlugin.class.getResourceAsStream(path)) {
            return in == null ? null : in.readAllBytes();
        } catch (IOException e) {
            LOGGER.warn("[cfx-compat-vehicles] Failed to read {}: {}", path, e.getMessage());
            return null;
        }
    }
}
