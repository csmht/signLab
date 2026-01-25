package com.example.demo.util;

import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class CryptoUtil {

    private static final String DEFAULT_KEY = "SLZ_CSMHT";
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

    private String key = DEFAULT_KEY;

    public void setKey(String key) {
        this.key = key;
    }

    public String encrypt(String plaintext) {
        try {
            byte[] keyBytes = padKey(key);
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("加密失败: " + e.getMessage(), e);
        }
    }

    public String decrypt(String ciphertext) {
        try {
            byte[] keyBytes = padKey(key);
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getUrlDecoder().decode(ciphertext));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("解密失败: " + e.getMessage(), e);
        }
    }

    private byte[] padKey(String key) {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] paddedKey = new byte[16];
        System.arraycopy(keyBytes, 0, paddedKey, 0, Math.min(keyBytes.length, 16));
        return paddedKey;
    }
}