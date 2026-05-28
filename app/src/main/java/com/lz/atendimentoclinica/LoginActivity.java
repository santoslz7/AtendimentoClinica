package com.lz.atendimentoclinica;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.lz.atendimentoclinica.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etSenha;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Se já está logado, vai direto pro dashboard
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            irParaDashboard();
            return;
        }

        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etSenha = findViewById(R.id.etSenha);

        Button btnEntrar    = findViewById(R.id.btnEntrar);
        Button btnCadastrar = findViewById(R.id.btnCadastrar);

        btnEntrar.setOnClickListener(v -> fazerLogin());
        btnCadastrar.setOnClickListener(v ->
                startActivity(new Intent(this, CadastroActivity.class)));
    }

    private void fazerLogin() {
        String email = etEmail.getText().toString().trim();
        String senha = etSenha.getText().toString().trim();

        if (email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, senha)
                .addOnSuccessListener(r -> irParaDashboard())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Email ou senha incorretos", Toast.LENGTH_SHORT).show());
    }

    private void irParaDashboard() {
        startActivity(new Intent(this, DashboardActivity.class));
        finish();
    }
}