package com.lz.atendimentoclinica;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.HashMap;
import java.util.Map;

public class HorariosActivity extends AppCompatActivity {

    private Spinner spinnerMedico, spinnerDia;
    private EditText etHoraInicio, etHoraFim;
    private FirebaseFirestore db;
    private String clinicaId;
    private final Map<String, String> medicosMap = new HashMap<>(); // nome -> id

    private final String[] diasSemana = {
            "Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_horarios);

        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Horários");

        db = FirebaseFirestore.getInstance();
        clinicaId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        spinnerMedico  = findViewById(R.id.spinnerMedico);
        spinnerDia     = findViewById(R.id.spinnerDia);
        etHoraInicio   = findViewById(R.id.etHoraInicio);
        etHoraFim      = findViewById(R.id.etHoraFim);
        Button btnSalvar = findViewById(R.id.btnSalvarHorario);
        btnSalvar.setOnClickListener(v -> {
        });

        // Popula spinner dos dias
        ArrayAdapter<String> adapterDias = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, diasSemana);
        adapterDias.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDia.setAdapter(adapterDias);

        carregarMedicos();

        findViewById(R.id.btnSalvarHorario).setOnClickListener(v -> salvarHorario());
    }

    private void carregarMedicos() {
        db.collection("clinicas").document(clinicaId).collection("medicos").get()
                .addOnSuccessListener(snap -> {
                    String[] nomes = new String[snap.size()];
                    int i = 0;
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        String nome = doc.getString("nome");
                        medicosMap.put(nome, doc.getId());
                        nomes[i++] = nome;
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_spinner_item, nomes);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerMedico.setAdapter(adapter);
                });
    }

    private void salvarHorario() {
        String medicoNome  = spinnerMedico.getSelectedItem().toString();
        String medicoId    = medicosMap.get(medicoNome);
        String dia         = spinnerDia.getSelectedItem().toString();
        String horaInicio  = etHoraInicio.getText().toString().trim();
        String horaFim     = etHoraFim.getText().toString().trim();

        if (horaInicio.isEmpty() || horaFim.isEmpty()) {
            Toast.makeText(this, "Preencha os horários", Toast.LENGTH_SHORT).show();
            return;
        }

        Horario horario = new Horario();
        horario.setMedicoId(medicoId);
        horario.setDiaSemana(dia);
        horario.setHoraInicio(horaInicio);
        horario.setHoraFim(horaFim);
        horario.setDisponivel(true);

        db.collection("clinicas").document(clinicaId)
                .collection("medicos").document(medicoId)
                .collection("horarios").add(horario)
                .addOnSuccessListener(r ->
                        Toast.makeText(this, "Horário salvo!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao salvar", Toast.LENGTH_SHORT).show());
    }
}