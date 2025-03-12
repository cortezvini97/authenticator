package com.vcinsidedigital.authenticator.activities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.vcinsidedigital.authenticator.R;
import com.vcinsidedigital.authenticator.helper.SecretDAO;
import com.vcinsidedigital.authenticator.model.Secret;
import com.vcinsidedigital.authenticator.util.CryptoUtil;
import com.vcinsidedigital.authenticator.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class AddSecretCodeActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextInputLayout nameInput, codeInput;
    private Spinner spinner;
    private TextView textViewSpinnerError;
    private Button buttonSave;
    private List<Secret> listSecretCodes;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView()).setAppearanceLightStatusBars(false);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

        setContentView(R.layout.activity_add_secret_code);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        toolbar = findViewById(R.id.toolbar);
        nameInput = findViewById(R.id.text_input_layout_name);
        codeInput = findViewById(R.id.text_input_layout_code);
        spinner = findViewById(R.id.spinner);
        textViewSpinnerError = findViewById(R.id.text_spinner_error);
        buttonSave = findViewById(R.id.button_save);

        setSupportActionBar(toolbar);


        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Adicionar Chave Secreta");
        actionBar.setDisplayHomeAsUpEnabled(true);


        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.spinner_items,  // O array definido no strings.xml
                android.R.layout.simple_spinner_item
        );

        // Especifica o layout a ser usado quando a lista aparecer
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Aplica o adapter ao Spinner
        spinner.setAdapter(adapter);

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameInput.getEditText().getText().toString();
                String code = codeInput.getEditText().getText().toString();
                String selectedItem = spinner.getSelectedItem().toString();

                if(name.isEmpty() || name.isBlank() || code.isEmpty() || code.isBlank() || selectedItem.equals("Selecione uma opção")){
                    if(name.isEmpty() || name.isBlank()){
                        nameInput.setError("Campo Nome obrigatório.");
                    }else{
                        nameInput.setError(null);
                    }

                    if(code.isEmpty() || code.isBlank()){
                        codeInput.setError("Campo Secret Code obrigatório.");
                    }else{
                        codeInput.setError(null);
                    }
                    if(selectedItem.equals("Selecione uma opção")){
                        textViewSpinnerError.setVisibility(View.VISIBLE);
                        textViewSpinnerError.setText("Selecione uma opção Válida.");
                    }else{
                        textViewSpinnerError.setVisibility(View.GONE);
                        textViewSpinnerError.setText(null);
                    }
                }else{
                    nameInput.setError(null);
                    codeInput.setError(null);
                    textViewSpinnerError.setVisibility(View.GONE);
                    textViewSpinnerError.setText(null);

                    String chaveCriptografada = null;
                    try {
                        chaveCriptografada = CryptoUtil.encrypt(code, getApplicationContext());

                        Secret secret = new Secret();
                        secret.setName(name);
                        secret.setCode(chaveCriptografada);
                        secret.setType(selectedItem);

                        SecretDAO dao = new SecretDAO(getApplicationContext());
                        listSecretCodes = dao.listAll();

                        if (listSecretCodes.size() > 0) {
                            for (int i = 0; i < listSecretCodes.size(); i++) {
                                if (listSecretCodes.get(i).getName().equals(name)) {
                                    Toast.makeText(getApplicationContext(), "Já existe uma chave com esse nome.", Toast.LENGTH_LONG).show();
                                    return;
                                }
                            }
                        }

                        if (secret.save(getApplicationContext())) {
                            Toast.makeText(getApplicationContext(), "Chave salva com sucesso.", Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "Erro ao salvar a chave.", Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "Erro ao salvar a chave.", Toast.LENGTH_LONG).show();
                    }
                }

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        sessionManager = SessionManager.getInstance(this);
        sessionManager.setCurrentActivity("add_secret_code_activity");
        if (!sessionManager.isLoggedIn()) {
            // Redireciona para a tela de login se o usuário não estiver logado
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}