package com.lz.atendimentoclinica;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lz.atendimentoclinica.databinding.ActivityCadastroBinding;

import java.util.HashMap;
import java.util.Map;

public class CadastroActivity extends AppCompatActivity {

    private ActivityCadastroBinding binding;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private TextInputEditText etNome, etCnpj, etEmail, etSenha, etTelefone;
    private TextInputEditText etCep, etLogradouro, etBairro, etCidade, etEstado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCadastroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        etNome       = binding.etNome;
        etCnpj       = binding.etCnpj;
        etEmail      = binding.etEmail;
        etSenha      = binding.etSenha;
        etTelefone   = binding.etTelefone;
        etCep        = binding.etCep;
        etLogradouro = binding.etLogradouro;
        etBairro     = binding.etBairro;
        etCidade     = binding.etCidade;
        etEstado     = binding.etEstado;

        etCep.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String cep = etCep.getText().toString().trim();
                if (cep.length() >= 8) {
                    android.view.inputmethod.InputMethodManager imm =
                            (android.view.inputmethod.InputMethodManager)
                                    getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                    if (imm != null) imm.hideSoftInputFromWindow(etCep.getWindowToken(), 0);

                    buscarCep(cep);
                }
            }
        });

        binding.btnCadastrar.setOnClickListener(v -> {
            String nome       = etNome.getText().toString().trim();
            String cnpj       = etCnpj.getText().toString().trim();
            String email      = etEmail.getText().toString().trim();
            String senha      = etSenha.getText().toString().trim();
            String telefone   = etTelefone.getText().toString().trim();
            String cep        = etCep.getText().toString().trim();
            String logradouro = etLogradouro.getText().toString().trim();
            String bairro     = etBairro.getText().toString().trim();
            String cidade     = etCidade.getText().toString().trim();
            String estado     = etEstado.getText().toString().trim();

            if (!validarCampos(nome, cnpj, cep, email, senha)) return;

            cadastrarClinica(nome, cnpj, email, senha, telefone,
                    cep, logradouro, bairro, cidade, estado);
        });

        // Volta para o LoginActivity
        binding.txtJaTenhoConta.setOnClickListener(v -> finish());
    }

    // -------------------------------------------------------------------------
    // Busca de CEP usando ApiService (OkHttp + callback próprio)
    // -------------------------------------------------------------------------
    private void buscarCep(String cep) {
        etCep.setError(null);

        ApiService.buscarCep(cep, new ApiService.CepCallback() {
            @Override
            public void onSuccess(String logradouro, String bairro, String cidade, String uf) {
                etLogradouro.setText(logradouro);
                etBairro.setText(bairro);
                etCidade.setText(cidade);
                etEstado.setText(uf);
            }

            @Override
            public void onError(String mensagem) {
                Toast.makeText(CadastroActivity.this, mensagem, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // -------------------------------------------------------------------------
    // Validação dos campos obrigatórios
    // -------------------------------------------------------------------------
    private boolean validarCampos(String nome, String cnpj, String cep,
                                  String email, String senha) {
        if (nome.isEmpty()) {
            etNome.setError("Informe o nome da clínica");
            etNome.requestFocus();
            return false;
        }
        if (cnpj.isEmpty()) {
            etCnpj.setError("Informe o CNPJ");
            etCnpj.requestFocus();
            return false;
        }
        if (!ApiService.validarCnpj(cnpj)) {
            etCnpj.setError("CNPJ inválido");
            etCnpj.requestFocus();
            return false;
        }
        if (cep.isEmpty() || cep.length() < 8) {
            etCep.setError("Informe um CEP válido");
            etCep.requestFocus();
            return false;
        }
        if (email.isEmpty()) {
            etEmail.setError("Informe o e-mail");
            etEmail.requestFocus();
            return false;
        }
        if (senha.isEmpty() || senha.length() < 6) {
            etSenha.setError("A senha deve ter no mínimo 6 caracteres");
            etSenha.requestFocus();
            return false;
        }
        return true;
    }

    // -------------------------------------------------------------------------
    // Criação do usuário no Firebase Auth
    // -------------------------------------------------------------------------
    private void cadastrarClinica(String nome, String cnpj, String email, String senha,
                                  String telefone, String cep, String logradouro,
                                  String bairro, String cidade, String estado) {
        binding.btnCadastrar.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();
                        salvarDadosNoFirestore(uid, nome, cnpj, email,
                                telefone, cep, logradouro, bairro, cidade, estado);
                    } else {
                        binding.btnCadastrar.setEnabled(true);
                        String erro = task.getException() != null
                                ? task.getException().getMessage()
                                : "Erro ao criar conta";
                        Toast.makeText(CadastroActivity.this, erro, Toast.LENGTH_LONG).show();
                    }
                });
    }

    // -------------------------------------------------------------------------
    // Persistência dos dados da clínica no Firestore
    // -------------------------------------------------------------------------
    private void salvarDadosNoFirestore(String uid, String nome, String cnpj, String email,
                                        String telefone, String cep, String logradouro,
                                        String bairro, String cidade, String estado) {
        Map<String, Object> clinica = new HashMap<>();
        clinica.put("id",         uid);
        clinica.put("nome",       nome);
        clinica.put("cnpj",       cnpj);
        clinica.put("email",      email);
        clinica.put("telefone",   telefone);
        clinica.put("cep",        cep);
        clinica.put("logradouro", logradouro);
        clinica.put("bairro",     bairro);
        clinica.put("cidade",     cidade);
        clinica.put("estado",     estado);
        clinica.put("criadoEm",   System.currentTimeMillis());

        db.collection("clinicas")
                .document(uid)
                .set(clinica)
                .addOnSuccessListener(unused -> {
                    mAuth.signOut();
                    Toast.makeText(CadastroActivity.this,
                            "Clínica cadastrada com sucesso!",
                            Toast.LENGTH_SHORT).show();
                    finish(); // fecha e volta para o LoginActivity
                })
                .addOnFailureListener(e -> {
                    binding.btnCadastrar.setEnabled(true);
                    Log.e("FIRESTORE", "Erro ao salvar dados da clínica", e);
                    Toast.makeText(CadastroActivity.this,
                            "Erro ao salvar dados: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}