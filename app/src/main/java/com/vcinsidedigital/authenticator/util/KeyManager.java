package com.vcinsidedigital.authenticator.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.security.Key;
import java.security.SecureRandom;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class KeyManager {

    private static final String PREF_NAME = "app_preferences";
    private static final String KEY_SECRET_KEY = "secret_key";

    // Método para gerar uma chave aleatória e salvar no SharedPreferences
    public static void generateAndSaveSecretKey(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // Verifica se já existe uma chave armazenada
        if (!sharedPreferences.contains(KEY_SECRET_KEY)) {
            try {
                // Gerar uma chave aleatória de 256 bits
                KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
                keyGenerator.init(256);  // Tamanho da chave
                SecretKey secretKey = keyGenerator.generateKey();

                // Converter a chave para Base64 para salvar como string
                String encodedKey = Base64.encodeToString(secretKey.getEncoded(), Base64.DEFAULT);

                // Salvar a chave no SharedPreferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(KEY_SECRET_KEY, encodedKey);
                editor.apply();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Método para recuperar a chave secreta
    public static String getSecretKey(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_SECRET_KEY, null);
    }
}