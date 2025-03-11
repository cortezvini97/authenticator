package com.vcinsidedigital.authenticator.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.vcinsidedigital.authenticator.R;
import com.vcinsidedigital.authenticator.adapter.SecretAdapter;
import com.vcinsidedigital.authenticator.helper.SecretDAO;
import com.vcinsidedigital.authenticator.model.Secret;
import com.vcinsidedigital.authenticator.util.CryptoUtil;
import com.vcinsidedigital.authenticator.util.SessionManager;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private FloatingActionButton fabMain, fabQRCode, fabAddCode;
    private LinearLayout groupFloatting;
    private RecyclerView recyclerView;
    boolean active = false;
    private List<Secret> listSecretCodes;
    private static final String BACKUP_FILE_NAME = "vcid_authentiator_backup";
    private static final int PICK_BACKUP_FILE_REQUEST_CODE = 1001;
    private static final int REQUEST_CODE_SAVE_FILE = 1002;

    private SessionManager sessionManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        sessionManager = SessionManager.getInstance(this);

        String currentActivity = getIntent().getStringExtra("currentActivity");

        if(currentActivity != null && currentActivity.equals("add_secret_code_activity")){
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, AddSecretCodeActivity.class);
            startActivity(intent);
        }else if(currentActivity != null && currentActivity.equals("code_gen_activity")){
            String secretEncrypted = sessionManager.getData("secret");

            if(secretEncrypted != null){
                try {
                    String secretB64 = CryptoUtil.decrypt(secretEncrypted);
                    byte[] secretBytes = Base64.decode(secretB64, Base64.DEFAULT);
                    ByteArrayInputStream bis = new ByteArrayInputStream(secretBytes);
                    ObjectInputStream in = new ObjectInputStream(bis);
                    Secret secret = (Secret) in.readObject();
                    sessionManager.removeData("secret");
                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this, CodeGenActivity.class);
                    intent.putExtra("secret", secret);
                    startActivity(intent);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }


        }

        setContentView(R.layout.activity_main);

        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView()).setAppearanceLightStatusBars(false);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

            return insets;
        });

        toolbar = findViewById(R.id.toolbarMainActivity);
        fabMain = findViewById(R.id.fab_main);
        fabQRCode = findViewById(R.id.fab_qr_code);
        fabAddCode = findViewById(R.id.fab_add_code);
        groupFloatting = findViewById(R.id.group_floatting);

        recyclerView = findViewById(R.id.recycler_view);



        setSupportActionBar(toolbar);

        fabMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animation();
            }
        });

        fabQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animation();
                scanQRCode();
            }
        });

        fabAddCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animation();
                Intent intent = new Intent(MainActivity.this, AddSecretCodeActivity.class);
                startActivity(intent);
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        this.loadSecretList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.config_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if(item.getItemId() == R.id.config_item){
            View view = findViewById(R.id.config_item);
            PopupMenu popupMenu = new PopupMenu(this, view);
            getMenuInflater().inflate(R.menu.config_app, popupMenu.getMenu());
            popupMenu.show();
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item)
                {
                    if(item.getItemId() == R.id.config_reset_app){
                        showDialogApp();
                        return true;
                    }else if(item.getItemId() == R.id.confg_backup){
                        if(listSecretCodes.size() == 0){
                            Toast.makeText(getApplicationContext(), "Não há dados para fazer backup.", Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        createBackup();  // Chama a função para criar o backup

                        return true;
                    }else if(item.getItemId() == R.id.config_restore){
                        loadBackup();
                        return true;
                    }
                    return false;
                }
            });
        }
        return super.onOptionsItemSelected(item);
    }

    public void loadSecretList(){
        SecretDAO secretDAO = new SecretDAO(this);
        listSecretCodes = secretDAO.listAll();


        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(), LinearLayout.VERTICAL));
        SecretAdapter adapter = new SecretAdapter(listSecretCodes,  MainActivity.this);
        recyclerView.setAdapter(adapter);

    }


    ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(), result -> {
        if(result != null){
            String authURL = result.getContents();
            if (authURL.startsWith("otpauth://")) {
                try {
                    // Extrair o nome (depois de "totp/" e antes do "?")
                    Uri uri = Uri.parse(authURL);
                    String path = uri.getPath(); // Exemplo: "/Cortez@vcid.com.br"
                    String name = path != null ? path.substring(1) : "Desconhecido"; // Remove a barra inicial

                    // Extrair a chave secreta
                    String chave = uri.getQueryParameter("secret");

                    String chaveCriptografada = CryptoUtil.encrypt(chave);

                    Secret secret = new Secret();
                    secret.setName(name);
                    secret.setCode(chaveCriptografada);
                    secret.setType("Google Authenticator");


                    if(secret.save(getApplicationContext())){
                        Toast.makeText(getApplicationContext(), "Código Salvo com Sucesso.", Toast.LENGTH_LONG).show();
                        loadSecretList();
                    }else {
                        Toast.makeText(getApplicationContext(), "Erro ao salvar o código.", Toast.LENGTH_SHORT).show();
                    }



                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Erro ao processar QR Code.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "QR Code inválido.", Toast.LENGTH_SHORT).show();
            }

        }
    });

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);



        if (requestCode == PICK_BACKUP_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            if (uri == null) {
                Toast.makeText(this, "Nenhum arquivo selecionado.", Toast.LENGTH_SHORT).show();
                return;
            }

            try (InputStream inputStream = getContentResolver().openInputStream(uri);
                 ObjectInputStream ois = new ObjectInputStream(inputStream)) {

                // Lê e descriptografa os dados
                String aesKey = (String) ois.readObject();
                String encryptedData = (String) ois.readObject();
                byte[] decryptedData = Base64.decode(CryptoUtil.decryptWithCustomKey(encryptedData, aesKey), Base64.DEFAULT);

                // Desserializa os dados
                ByteArrayInputStream bis = new ByteArrayInputStream(decryptedData);
                ObjectInputStream in = new ObjectInputStream(bis);
                List<Secret> listSecretCodes = (List<Secret>) in.readObject();

                List<Secret> newListSecret = new ArrayList<>();

                // Salva os dados restaurados no banco de dados
                SecretDAO dao = new SecretDAO(getApplicationContext());
                for (Secret secret : listSecretCodes) {
                    String secretCodeCrypted = secret.getCode();
                    Log.i("DEBUG_SECRET", secretCodeCrypted);
                    try {
                        byte[] decryptedSecretCodeByte = Base64.decode(CryptoUtil.decryptWithCustomKey(secretCodeCrypted, aesKey), Base64.DEFAULT);
                        String secretCode = new String(decryptedSecretCodeByte);
                        String secretEncrypted = CryptoUtil.encrypt(secretCode);
                        secret.setCode(secretEncrypted);
                        newListSecret.add(secret);
                    } catch (Exception e) {
                        Toast.makeText(this, "Erro ao descriptografar o código.", Toast.LENGTH_SHORT).show();
                    }
                }

                for (Secret secret : newListSecret) {
                    dao.save(secret);
                }

                // Atualiza a RecyclerView
                loadSecretList();

                Toast.makeText(this, "Backup restaurado com sucesso!", Toast.LENGTH_SHORT).show();

            } catch (FileNotFoundException e) {
                Toast.makeText(getApplicationContext(), "Arquivo não encontrado", Toast.LENGTH_LONG).show();
                Log.e("ERROR_RESTORE", "Arquivo não encontrado", e);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("ERROR_RESTORE", "Erro ao restaurar backup: " + e.getMessage(), e);
                Toast.makeText(this, "Erro ao restaurar backup", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == REQUEST_CODE_SAVE_FILE && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                saveBackupToUri(uri);
            }
        }
    }



    @Override
    protected void onResume() {
        super.onResume();
        Log.i("RESUME_DEBUG", "onResume");

        sessionManager.setCurrentActivity("main_activity");

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
        Log.i("STOP_DEBUG", "onDestroy");
    }

    private void showDialogApp(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Atenção");
        builder.setMessage("Isso deletará todos os dados do aplicativo. Recomendamos que você faça um backup antes de continuar.");
        builder.setPositiveButton("Continuar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(SecretDAO.deleteDatabse(getApplicationContext())) {
                    finish();
                    Intent i = new Intent(MainActivity.this, MainActivity.class);
                    startActivity(i);
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


    private void createBackup() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HHmmss", Locale.getDefault());
        String dateTime = dateFormat.format(new Date());
        String fileName = BACKUP_FILE_NAME + "_" + dateTime + ".dat";

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/octet-stream"); // Tipo genérico para arquivos binários
        intent.putExtra(Intent.EXTRA_TITLE, fileName);

        startActivityForResult(intent, REQUEST_CODE_SAVE_FILE);
    }

    private void saveBackupToUri(Uri uri) {
        List<Secret> newListSecret = new ArrayList<>();

        String aesKey;


        try {
            aesKey = CryptoUtil.generateRandomAESKey();
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(this, "Erro ao gerar chave AES", Toast.LENGTH_SHORT).show();
            return;
        }

        // Descriptografar os códigos antes de salvar
        for (Secret secret : listSecretCodes) {
            try {
                String secretCode = CryptoUtil.decrypt(secret.getCode());
                byte[] secretCodeBytes = secretCode.getBytes();
                String secretCodeB64 = Base64.encodeToString(secretCodeBytes, Base64.DEFAULT);
                String cryptedCode = CryptoUtil.encryptWithCustomKey(secretCodeB64, aesKey);
                secret.setCode(cryptedCode);
                newListSecret.add(secret);
            } catch (Exception e) {
                Toast.makeText(this, "Erro ao descriptografar o código.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        try {
            // Serializar os dados
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(newListSecret);
            out.flush();
            byte[] serializedData = bos.toByteArray();

            // Criptografar os dados

            String encryptedData = CryptoUtil.encryptWithCustomKey(Base64.encodeToString(serializedData, Base64.DEFAULT), aesKey);

            // Escrever no arquivo
            try (OutputStream outputStream = getContentResolver().openOutputStream(uri);
                 ObjectOutputStream oos = new ObjectOutputStream(outputStream)) {

                oos.writeObject(aesKey);
                oos.writeObject(encryptedData);
                oos.flush();

                Toast.makeText(this, "Backup salvo com sucesso!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao salvar backup", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadBackup() {
        SecretDAO.deleteDatabse(getApplicationContext());

        // Inicia uma intent para que o usuário escolha um arquivo de backup
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");  // Permite qualquer tipo de arquivo
        //intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Inicia a atividade de seleção de arquivos
        startActivityForResult(intent, PICK_BACKUP_FILE_REQUEST_CODE);

    }




    private void scanQRCode(){
        ScanOptions options = new ScanOptions();
        options.setPrompt("Aponte a Câmera para o QR Code.");
        options.setBeepEnabled(false);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barcodeLauncher.launch(options);
    }

    private void animation(){
        if(active){
            // Animação de rotação do FAB
            ObjectAnimator rotateAnim = ObjectAnimator.ofFloat(fabMain, "rotation", 45f, 0f);
            rotateAnim.setDuration(200);
            rotateAnim.start();

            // Animação para esconder a groupFloatting com alpha
            ObjectAnimator fadeOutAnim = ObjectAnimator.ofFloat(groupFloatting, "alpha", 1f, 0f);
            fadeOutAnim.setDuration(200);
            fadeOutAnim.start();

            fadeOutAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    groupFloatting.setVisibility(View.GONE); // Defina como GONE após a animação
                }
            });

            active = false;
        } else {
            // Animação de rotação do FAB
            ObjectAnimator rotateAnim = ObjectAnimator.ofFloat(fabMain, "rotation", 0f, 45f);
            rotateAnim.setDuration(200);
            rotateAnim.start();

            // Animação para mostrar a groupFloatting com alpha
            groupFloatting.setVisibility(View.VISIBLE);
            ObjectAnimator fadeInAnim = ObjectAnimator.ofFloat(groupFloatting, "alpha", 0f, 1f);
            fadeInAnim.setDuration(200);
            fadeInAnim.start();

            active = true;
        }
    }
}
