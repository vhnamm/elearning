package com.hnv.elearning.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Slf4j
public final class CryptoUtil {
    public static String sha256(String raw){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return raw;
    }
}
