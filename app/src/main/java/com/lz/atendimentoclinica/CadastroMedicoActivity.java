package com.lz.atendimentoclinica;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class CadastroMedicoActivity extends AppCompatActivity {

    private TextInputEditText etNome, etCrm, etEspecialidade;
    private FirebaseFirestore db;
    private String clinicaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro); // CORRIGIDO: layout próprio

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Novo Médico");
        }

        db = FirebaseFirestore.getInstance();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            clinicaId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        etNome         = findViewById(R.id.etNome);
        etCrm          = findViewById(R.id.etCrm);
        etEspecialidade = findViewById(R.id.etEspecialidade);

        Button btnSalvar = findViewById(R.id.btnSalvar);
        btnSalvar.setOnClickListener(v -> salvar());
    }

    private void salvar() {
        String nome         = etNome.getText().toString().trim();
        String crm          = etCrm.getText().toString().trim();
        String especialidade = etEspecialidade.getText().toString().trim();

        if (nome.isEmpty() || crm.isEmpty() || especialidade.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (clinicaId == null) {
            Toast.makeText(this, "Erro: Clínica não autenticada.", Toast.LENGTH_SHORT).show();
            return;
        }

        Medico medico = new Medico();
        medico.setNome(nome);
        medico.setCrm(crm);
        medico.setEspecialidade(especialidade);
        medico.setClinicaId(clinicaId);

        db.collection("clinicas").document(clinicaId)
                .collection("medicos")
                .add(medico)
                .addOnSuccessListener(ref -> {
                    Toast.makeText(this, "Médico salvo com sucesso!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao salvar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}