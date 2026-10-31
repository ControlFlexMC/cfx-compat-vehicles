package com.ifels.cfx.vehicles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * 把本模组自带的 compat JSON 放进 {@code config/controlflex/compat/mods/}。
 *
 * <p>协议与 {@code cfx-compat-epicfight} 一致：只有 SHA-1 与 jar 内资源不同才覆盖，
 * 绝不碰 {@code compat/user/}（那是玩家自己的配置，优先级更高）。</p>
 */
final class CompatConfigInstaller {

    static final String COMPAT_FILE_NAME = "mts.json";
    static final String LEGACY_FILE_NAME = "mts_keys.json";
    static final String MODS_RELATIVE = "config/controlflex/compat/mods";

    private CompatConfigInstaller() {
    }

    static String sha1Hex(byte[] data) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-1").digest(data));
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    /**
     * @return false 表示无内容可比对（调用方不应安装）；true 表示需要写：目标缺失、
     *         不可读、哈希算不出来、或哈希与 jar 内不一致。
     */
    static boolean needsOverwrite(byte[] bundled, Path dest) {
        if (bundled == null) {
            return false;
        }
        String expected = sha1Hex(bundled);
        if (expected == null) {
            return true;
        }
        if (!Files.isRegularFile(dest)) {
            return true;
        }
        try {
            String actual = sha1Hex(Files.readAllBytes(dest));
            return actual == null || !expected.equals(actual);
        } catch (IOException e) {
            return true;
        }
    }

    /** 删掉旧格式残留（{@code mts_keys.json}）；不存在也算成功。 */
    static boolean deleteLegacyFile(Path modsDir) {
        if (modsDir == null) {
            return true;
        }
        try {
            Files.deleteIfExists(modsDir.resolve(LEGACY_FILE_NAME));
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
