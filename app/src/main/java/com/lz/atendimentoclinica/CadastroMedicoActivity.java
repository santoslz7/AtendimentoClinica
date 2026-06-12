package com.lz.atendimentoclinica;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CadastroMedicoActivity extends AppCompatActivity {

    private TextInputEditText etNome, etCrm;
    private AutoCompleteTextView autoCompleteEspecialidade;
    private FirebaseFirestore db;
    private String clinicaId;

    // Lista global para controlar as especialidades que já existem no Firebase
    private List<String> listaEspecialidades = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_medico);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Novo Médico");
        }

        db = FirebaseFirestore.getInstance();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            clinicaId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        etNome         = findViewById(R.id.etNome);
        etCrm          = findViewById(R.id.etCrm);
        autoCompleteEspecialidade = findViewById(R.id.autoCompleteEspecialidade);

        carregarEspecialidadesDoFirebase();

        Button btnSalvar = findViewById(R.id.btnSalvar);
        btnSalvar.setOnClickListener(v -> salvar());
    }

    private void carregarEspecialidadesDoFirebase() {
        db.collection("especialidade")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Limpa a lista antes de preencher para evitar duplicados caso recarregue
                    listaEspecialidades.clear();

                    // Percorre todos os documentos retornados do Firebase
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String nomeEspecialidade = document.getString("nome");
                        if (nomeEspecialidade != null) {
                            listaEspecialidades.add(nomeEspecialidade);
                        }
                    }

                    // Cria o adaptador com a lista vinda do Firebase
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            CadastroMedicoActivity.this,
                            android.R.layout.simple_dropdown_item_1line,
                            listaEspecialidades
                    );

                    // Vincula as opções ao seu AutoCompleteTextView
                    autoCompleteEspecialidade.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao carregar especialidades", Toast.LENGTH_SHORT).show();
                });
    }

    private void salvar() {
        String nome         = etNome.getText().toString().trim();
        String crm          = etCrm.getText().toString().trim();
        String especialidadeInput = autoCompleteEspecialidade.getText().toString().trim();

        if (nome.isEmpty() || crm.isEmpty() || especialidadeInput.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (clinicaId == null) {
            Toast.makeText(this, "Erro: Clínica não autenticada.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Formata o texto: Garante que a primeira letra fique maiúscula (ex: "cardiologia" -> "Cardiologia")
        String especialidadeFormatada = especialidadeInput.substring(0, 1).toUpperCase() + especialidadeInput.substring(1);

        // Se a especialidade digitada NÃO existe na lista do Firebase, salva ela na coleção global
        if (!listaEspecialidades.contains(especialidadeFormatada)) {
            Map<String, Object> novaEspecialidade = new HashMap<>();
            novaEspecialidade.put("nome", especialidadeFormatada);

            db.collection("especialidade")
                    .add(novaEspecialidade)
                    .addOnSuccessListener(documentReference -> {
                        // Adiciona na lista local para não tentar adicionar de novo se clicar rápido duas vezes
                        listaEspecialidades.add(especialidadeFormatada);
                    });
        }

        Medico medico = new Medico();
        medico.setNome(nome);
        medico.setCrm(crm);
        medico.setEspecialidade(especialidadeFormatada);
        medico.setClinicaId(clinicaId);

        db.collection("clinicas").document(clinicaId)
                .collection("medicos")
                .add(medico)
                .addOnSuccessListener(ref -> {
                    // Pega o ID gerado automaticamente na subcoleção
                    String medicoId = ref.getId();
                    ref.update("id", medicoId);

                    medico.setId(medicoId);
                    db.collection("medicos").document(medicoId)
                            .set(medico)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Médico salvo com sucesso!", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Erro ao espelhar na raiz: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao salvar na clínica: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}