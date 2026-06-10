package com.example.exchangerate.utils;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Slf4j
public class ExchangeRateCryptoUtil {

    public static String encrypt(String plainText, String key) {
        try {
            byte[] keyBytes = key.getBytes("UTF-8");
            byte[] key16 = new byte[16];
            System.arraycopy(keyBytes, 0, key16, 0, Math.min(keyBytes.length, 16));

            SecretKeySpec secretKey = new SecretKeySpec(key16, "AES");
            IvParameterSpec iv = new IvParameterSpec(key16);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);

            byte[] encrypted = cipher.doFinal(plainText.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("Encryption failed", e);
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public static String decrypt(String encryptedText, String key) {
        try {
            byte[] keyBytes = key.getBytes("UTF-8");
            byte[] key16 = new byte[16];
            System.arraycopy(keyBytes, 0, key16, 0, Math.min(keyBytes.length, 16));

            SecretKeySpec secretKey = new SecretKeySpec(key16, "AES");
            IvParameterSpec iv = new IvParameterSpec(key16);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);

            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            return new String(decrypted, "UTF-8");
        } catch (Exception e) {
            log.error("Decryption failed", e);
            throw new RuntimeException("Decryption failed", e);
        }
    }
}