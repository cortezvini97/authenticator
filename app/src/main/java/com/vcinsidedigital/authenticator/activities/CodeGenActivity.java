package com.vcinsidedigital.authenticator.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.vcinsidedigital.authenticator.R;
import com.vcinsidedigital.authenticator.helper.SecretDAO;
import com.vcinsidedigital.authenticator.model.Secret;
import com.vcinsidedigital.authenticator.util.CryptoUtil;
import com.vcinsidedigital.authenticator.util.ResourceUtil;
import com.vcinsidedigital.authenticator.util.SessionManager;
import com.vcinsidedigital.authenticator.util.TOTP;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Hex;

import java.io.IOException;
import java.util.List;

public class CodeGenActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private Secret secret;
    private TextView nameView, detalhesView, textViewIcon, textTimer, textCode;
    private ImageView imageView;

    private SessionManager sessionManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView()).setAppearanceLightStatusBars(false);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

        setContentView(R.layout.activity_code_gen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        toolbar = findViewById(R.id.toolbar);
        nameView = findViewById(R.id.text_view_name);
        detalhesView = findViewById(R.id.text_view_detalhes);
        imageView = findViewById(R.id.image_icon);
        textViewIcon = findViewById(R.id.avatarText);
        textTimer = findViewById(R.id.text_timer);
        textCode = findViewById(R.id.text_code);


        setSupportActionBar(toolbar);


        Intent intent = getIntent();

        if(intent.getSerializableExtra("secret") != null){
            secret = (Secret) intent.getSerializableExtra("secret");
        }

        ActionBar actionBar = getSupportActionBar();
        if(secret != null){
            String name = secret.getName();
            String accountName = secret.getAccountName();
            Secret secretNew = secret.createIssuer();
            String issuer = secretNew.getIssuer();

            if(name.contains(":")){
                nameView.setText(issuer); // Parte antes do ":"
                detalhesView.setText(accountName); // Parte depois do ":"
            }else {
                nameView.setText(name); // Parte antes do ":"
                detalhesView.setText(name);
            }

            if(ResourceUtil.getIcon(issuer) != 0){
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageResource(ResourceUtil.getIcon(issuer));
            }else {
                textViewIcon.setVisibility(View.VISIBLE);
                textViewIcon.setText(issuer.substring(0,2).toUpperCase());
            }
        }
        actionBar.setTitle("");
        actionBar.setDisplayHomeAsUpEnabled(true);


        if(secret != null) {
            updateOTP();
            startTimer();
        }

        textCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = textCode.getText().toString().replace(" ", ""); // Remove espaços

                if (!code.isEmpty()) {
                    // Copia para a área de transferência
                    ClipboardManager clipboard = (ClipboardManager) v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Código", code);
                    clipboard.setPrimaryClip(clip);

                    Snackbar.make(v, "Código copiado.", Snackbar.LENGTH_LONG).show();
                } else {
                    Snackbar.make(v, "Código inválido.", Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    private static long getStep() {
        // 30 seconds StepSize (ID TOTP)
        return System.currentTimeMillis() / 30000;
    }

    private void startTimer() {
        // Calcular o tempo restante para o próximo intervalo de 30 segundos
        long currentStep = getStep();
        long millisUntilNextStep = (30000 - (System.currentTimeMillis() % 30000));

        // Iniciar o timer para contar até o próximo "step"
        new CountDownTimer(millisUntilNextStep, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Mostrar o tempo restante
                long secondsRemaining = millisUntilFinished / 1000;
                textTimer.setText("Expira em: " + secondsRemaining + "s");
            }

            @Override
            public void onFinish() {
                // Após o timer terminar, reiniciar a contagem para o próximo ciclo
                startTimer();
                updateOTP();

            }
        }.start();
    }

    private void updateOTP() {
        // Gerar o código TOTP
        if (secret != null && secret.getCode() != null) {
            long step = getStep();
            String otp = getTOTPCode(secret.getCode(), step);
            textCode.setText(otp); // Atualiza o TextView com o código gerado
        }
    }

    public String getTOTPCode(String secretKey, long step) {
        try{
            String decrypt_key = CryptoUtil.decrypt(secretKey, getApplicationContext());
            Base32 base32 = new Base32();
            byte[] bytes = base32.decode(decrypt_key);
            String hexKey = Hex.encodeHexString(bytes);
            return TOTP.getOTP(step, hexKey);
        }catch (Exception e){
            return "          ";
        }

    }

    private void showUpdateDialog(){
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_input, null);

        TextInputLayout nameInput = dialogView.findViewById(R.id.text_input_layout_name_edit);

        nameInput.getEditText().setText(secret.getName());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Digite um Nome");
        builder.setView(dialogView);
        builder.setPositiveButton("Salvar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = nameInput.getEditText().getText().toString();
                secret.setName(name);
                SecretDAO dao = new SecretDAO(getApplicationContext());
                List<Secret> secrets = dao.listAll();

                for (Secret s : secrets) {
                    if (s.getName().equals(name) && !s.getId().equals(secret.getId())) {
                        Toast.makeText(getApplicationContext(), "Nome já existe", Toast.LENGTH_LONG).show();
                        return;
                    }
                }

                if(secret.save(getApplicationContext())){
                    imageView.setVisibility(View.GONE);
                    textViewIcon.setVisibility(View.GONE);
                    String secretName = secret.getName();
                    String accountName = secret.getAccountName();
                    Secret secretNew = secret.createIssuer();
                    String issuer = secretNew.getIssuer();

                    if(name.contains(":")){
                        nameView.setText(issuer); // Parte antes do ":"
                        detalhesView.setText(accountName); // Parte depois do ":"
                    }else {
                        nameView.setText(name); // Parte antes do ":"
                        detalhesView.setText(name);
                    }

                    if(ResourceUtil.getIcon(issuer) != 0){
                        imageView.setVisibility(View.VISIBLE);
                        imageView.setImageResource(ResourceUtil.getIcon(issuer));
                    }else {
                        textViewIcon.setVisibility(View.VISIBLE);
                        textViewIcon.setText(issuer.substring(0,2).toUpperCase());
                    }

                    Toast.makeText(getApplicationContext(), "Salvo com sucesso", Toast.LENGTH_LONG).show();
                }else {
                    Toast.makeText(getApplicationContext(), "Erro ao salvar", Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    @Override
    protected void onResume() {
        super.onResume();

        sessionManager = SessionManager.getInstance(this);
        sessionManager.setCurrentActivity("code_gen_activity");
        if (secret != null) {
            byte[] secretData;
            try {
                secretData = secret.getData();
                String secretB64 = Base64.encodeToString(secretData, Base64.DEFAULT);
                String secretEncrypted = CryptoUtil.encrypt(secretB64, getApplicationContext());
                sessionManager.setData("secret", secretEncrypted);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        if (!sessionManager.isLoggedIn()) {
            // Redireciona para a tela de login
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("DESTROY_DEBUG", "onDestroy");
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.config_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.config_item){
            View view = findViewById(R.id.config_item);
            PopupMenu popupMenu = new PopupMenu(this, view);
            getMenuInflater().inflate(R.menu.gen_code_config, popupMenu.getMenu());
            popupMenu.show();
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if(item.getItemId() == R.id.edit_secret){
                        showUpdateDialog();
                        return true;
                    }else if(item.getItemId() == R.id.delete_secret){
                        SecretDAO dao = new SecretDAO(getApplicationContext());
                        if(dao.delete(secret)){
                            Toast.makeText(getApplicationContext(), "Deletado com sucesso", Toast.LENGTH_LONG).show();
                            finish();
                        }else {
                            Toast.makeText(getApplicationContext(), "Erro ao deletar", Toast.LENGTH_LONG).show();
                        }
                        return true;
                    }
                    return false;
                }
            });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}