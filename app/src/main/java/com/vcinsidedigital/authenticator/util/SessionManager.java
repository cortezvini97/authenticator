package com.vcinsidedigital.authenticator.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private static SessionManager instance;

    public SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
        return instance;
    }

    public void loginUser() {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    public void logoutUser() {
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void setCurrentActivity(String current_activity){
        editor.putString("current_activity", current_activity);
        editor.apply();
    }

    public String getCurrentActivity(){
        return sharedPreferences.getString("current_activity", "main_activity");
    }

    public void removeCurrentActivity(){
        editor.remove("current_activity");
        editor.apply();
    }

    public void setData(String key, String value){
        editor.putString(key, value);
        editor.apply();
    }

    public String getData(String key){
        return sharedPreferences.getString(key, "");
    }

    public void removeData(String key){
        editor.remove(key);
        editor.apply();
    }
}

