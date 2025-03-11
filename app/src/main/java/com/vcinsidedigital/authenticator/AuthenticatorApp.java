package com.vcinsidedigital.authenticator;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.vcinsidedigital.authenticator.util.SessionManager;

public class AuthenticatorApp extends Application implements DefaultLifecycleObserver
{
    private SessionManager sessionManager;
    public static boolean isAppAlreadyRunning = false;

    @Override
    public void onCreate() {
        super.onCreate();
        sessionManager = new SessionManager(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
                if(activity.getClass().getName().equals("com.vcinsidedigital.authenticator.activities.MainActivity")  && !isAppAlreadyRunning){
                    isAppAlreadyRunning = true;
                }
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {

            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {

            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {

            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {

            }
        });
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onStop(owner);
        Log.i("Lifecycle", "APP BACKGROUNDED");
        sessionManager.logoutUser();
    }
}