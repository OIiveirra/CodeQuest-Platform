package com.codequest.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * MD5 加密工具类，用于密码摘要计算。
 * Author: 张雨泽
 */
public final class MD5Utils {

    private MD5Utils() {
    }

    public static String md5(String rawText) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest((rawText == null ? "" : rawText).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to generate MD5 hash.", ex);
        }
    }
}
