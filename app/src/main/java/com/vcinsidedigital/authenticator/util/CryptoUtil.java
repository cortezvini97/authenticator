package com.vcinsidedigital.authenticator.util;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;


import com.vcinsidedigital.authenticator.BuildConfig;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoUtil {

    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final String KEY_ALIAS = BuildConfig.KEY_ALIAS;
    private static final String AES_MODE = "AES/GCM/NoPadding";
    private static final int AES_KEY_SIZE = 256;
    private static final int IV_SIZE = 12; // IV do GCM geralmente tem 12 bytes
    private static final int TAG_LENGTH = 128; // Tamanho do GCM Tag (em bits)

    // Gera uma chave AES no Keystore, se ainda não existir
    private static SecretKey getSecretKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        keyStore.load(null);

        if (!keyStore.containsAlias(KEY_ALIAS)) {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE);
            KeyGenParameterSpec keySpec = new KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT
            )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build();
            keyGenerator.init(keySpec);
            keyGenerator.generateKey();
        }

        return ((SecretKey) keyStore.getKey(KEY_ALIAS, null));
    }

    // Método para criptografar
    public static String encrypt(String plainText) throws Exception {
        SecretKey secretKey = getSecretKey();
        Cipher cipher = Cipher.getInstance(AES_MODE);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] iv = cipher.getIV();
        byte[] encryptedData = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        // Concatenar IV + DADOS e converter para Base64
        byte[] combined = new byte[iv.length + encryptedData.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encryptedData, 0, combined, iv.length, encryptedData.length);

        return Base64.encodeToString(combined, Base64.DEFAULT);
    }

    // Método para descriptografar
    public static String decrypt(String encryptedText) throws Exception {
        SecretKey secretKey = getSecretKey();
        byte[] combined = Base64.decode(encryptedText, Base64.DEFAULT);

        // Extrair IV e dados criptografados
        byte[] iv = new byte[12]; // IV do AES/GCM geralmente tem 12 bytes
        byte[] encryptedData = new byte[combined.length - 12];
        System.arraycopy(combined, 0, iv, 0, 12);
        System.arraycopy(combined, 12, encryptedData, 0, encryptedData.length);

        Cipher cipher = Cipher.getInstance(AES_MODE);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(128, iv));
        byte[] decryptedData = cipher.doFinal(encryptedData);

        return new String(decryptedData, StandardCharsets.UTF_8);
    }

    public static String generateRandomAESKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(AES_KEY_SIZE);
        SecretKey secretKey = keyGenerator.generateKey();
        return Base64.encodeToString(secretKey.getEncoded(), Base64.DEFAULT);
    }

    // Método para criptografar usando uma chave personalizada (sem Keystore)
    public static String encryptWithCustomKey(String plainText, String base64Key) throws Exception {
        byte[] keyBytes = Base64.decode(base64Key, Base64.DEFAULT);
        SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");

        Cipher cipher = Cipher.getInstance(AES_MODE);
        byte[] iv = new byte[IV_SIZE];
        new SecureRandom().nextBytes(iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH, iv));

        byte[] encryptedData = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        byte[] combined = new byte[iv.length + encryptedData.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encryptedData, 0, combined, iv.length, encryptedData.length);

        return Base64.encodeToString(combined, Base64.DEFAULT);
    }

    // Método para descriptografar usando uma chave personalizada (sem Keystore)
    public static String decryptWithCustomKey(String encryptedText, String base64Key) throws Exception {
        byte[] keyBytes = Base64.decode(base64Key, Base64.DEFAULT);
        SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");

        byte[] combined = Base64.decode(encryptedText, Base64.DEFAULT);

        byte[] iv = new byte[IV_SIZE];
        byte[] encryptedData = new byte[combined.length - IV_SIZE];
        System.arraycopy(combined, 0, iv, 0, IV_SIZE);
        System.arraycopy(combined, IV_SIZE, encryptedData, 0, encryptedData.length);

        Cipher cipher = Cipher.getInstance(AES_MODE);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH, iv));
        byte[] decryptedData = cipher.doFinal(encryptedData);

        return new String(decryptedData, StandardCharsets.UTF_8);
    }
}