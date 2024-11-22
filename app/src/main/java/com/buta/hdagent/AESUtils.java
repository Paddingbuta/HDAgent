package com.buta.hdagent;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import android.util.Base64;
import java.nio.charset.StandardCharsets;

public class AESUtils {
    private static final String CHARSET = "UTF-8";
    private static String INIT_VECTOR = LoadLib.getA();
    private static String SECRET_KEY = LoadLib.getB();

    // ECB Mode Encryption
    public static String encryptECB(String value) {
        try {

            byte[] key = getSecretKeySpec(SECRET_KEY);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"));
            byte[] encryptedBytes = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));

            return Base64.encodeToString(encryptedBytes, Base64.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting with ECB mode", e);
        }
    }

    // ECB Mode Decryption
    public static String decryptECB(String encryptedValue) {
        try {
            byte[] key = getSecretKeySpec(SECRET_KEY);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"));
            byte[] decryptedBytes = cipher.doFinal(Base64.decode(encryptedValue, Base64.DEFAULT));
            return new String(decryptedBytes, StandardCharsets.UTF_8).trim();
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting with ECB mode", e);
        }
    }

    // CBC Mode Encryption
    public static String encryptCBC(String value) {
        try {
            byte[] key = getSecretKeySpec(SECRET_KEY);
            IvParameterSpec ivSpec = new IvParameterSpec(INIT_VECTOR.getBytes(StandardCharsets.UTF_8));
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), ivSpec);
            byte[] encryptedBytes = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(encryptedBytes, Base64.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting with CBC mode", e);
        }
    }

    // CBC Mode Decryption
    public static String decryptCBC(String encryptedValue) {
        try {
            byte[] key = getSecretKeySpec(SECRET_KEY);
            IvParameterSpec ivSpec = new IvParameterSpec(INIT_VECTOR.getBytes(StandardCharsets.UTF_8));
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), ivSpec);
            byte[] decryptedBytes = cipher.doFinal(Base64.decode(encryptedValue, Base64.DEFAULT));
            return new String(decryptedBytes, StandardCharsets.UTF_8).trim();
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting with CBC mode", e);
        }
    }
    public static byte[] getSecretKeySpec(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(password.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("Error generating secret key", e);
        }
    }
}
