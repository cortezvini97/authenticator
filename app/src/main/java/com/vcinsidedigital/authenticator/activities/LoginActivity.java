package com.vcinsidedigital.authenticator.activities;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.vcinsidedigital.authenticator.AuthenticatorApp;
import com.vcinsidedigital.authenticator.R;
import com.vcinsidedigital.authenticator.util.KeyManager;
import com.vcinsidedigital.authenticator.util.SessionManager;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;
import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK;

import java.util.concurrent.Executor;

public class LoginActivity extends AppCompatActivity
{

    private Button btnAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);



        setContentView(R.layout.activity_login);

        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView()).setAppearanceLightStatusBars(false);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

            return insets;
        });


        KeyManager.generateAndSaveSecretKey(this);

        btnAuth = findViewById(R.id.btn_auth);

        btnAuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verefyBiometry();
            }
        });

        verefyBiometry();
    }



    private void verefyBiometry(){
        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate(BIOMETRIC_STRONG)){
            case BiometricManager.BIOMETRIC_SUCCESS:
                createAuth();
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Toast.makeText(LoginActivity.this, "Sensor de biometria não encontrado!", Toast.LENGTH_SHORT).show();
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                // Caso o hardware de biometria esteja indisponível
                Toast.makeText(LoginActivity.this, "Sensor de biometria não disponível!", Toast.LENGTH_SHORT).show();
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Toast.makeText(LoginActivity.this, "Biometria não cadastrada!", Toast.LENGTH_SHORT).show();
                final Intent biometryConfig = new Intent(Settings.ACTION_BIOMETRIC_ENROLL);
                biometryConfig.putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED, BIOMETRIC_STRONG);
                startActivityForResult(biometryConfig, 1000);
                break;
        }
    }


    private void createAuth(){
        Executor executor = ContextCompat.getMainExecutor(this);

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Authenticator")
                .setSubtitle("Use sua vereficação biométrica ou o bloqueio de tela para continuar")
                .setAllowedAuthenticators(BIOMETRIC_STRONG | DEVICE_CREDENTIAL | BIOMETRIC_WEAK)
                .setConfirmationRequired(false)
                .build();

        BiometricPrompt authReadBiometric = new BiometricPrompt(LoginActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);

                SessionManager sessionManager = SessionManager.getInstance(LoginActivity.this);
                sessionManager.loginUser();


                String current_activity;

                if (AuthenticatorApp.isAppAlreadyRunning) {
                    Log.d("AppStatus", "O aplicativo já estava aberto.");
                    current_activity = sessionManager.getCurrentActivity();
                } else {
                    Log.d("AppStatus", "O aplicativo está iniciando agora.");
                    sessionManager.removeCurrentActivity();
                    current_activity = "main_activity";
                }

                Log.i("ACTIVITY_CURRENT", current_activity);

                if(current_activity == null || current_activity.equals("main_activity")){
                    Intent intent = new Intent();
                    intent.setClass(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }else if(current_activity.equals("add_secret_code_activity")){
                    Intent intent = new Intent();
                    intent.setClass(LoginActivity.this, MainActivity.class);
                    intent.putExtra("currentActivity", "add_secret_code_activity");
                    startActivity(intent);
                    finish();
                }else if(current_activity.equals("code_gen_activity")){
                    Intent intent = new Intent();
                    intent.setClass(LoginActivity.this, MainActivity.class);
                    intent.putExtra("currentActivity", "code_gen_activity");
                    startActivity(intent);
                    finish();
                }


            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
            }
        });

        authReadBiometric.authenticate(promptInfo);
    }
}