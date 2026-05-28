package com.lz.atendimentoclinica;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PacientesActivity extends AppCompatActivity {

    private PacienteAdapter adapter;
    private final List<String> pacientesNomes = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_generica);

        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Pacientes");

        db = FirebaseFirestore.getInstance();
        String clinicaId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        RecyclerView recycler = findViewById(R.id.recyclerView);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PacienteAdapter(pacientesNomes);
        recycler.setAdapter(adapter);

        // Busca pacientes únicos que agendaram com esta clínica
        db.collection("consultas")
                .whereEqualTo("clinicaId", clinicaId)
                .get()
                .addOnSuccessListener(snap -> {
                    Set<String> idsUnicos = new HashSet<>();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        String pid = doc.getString("pacienteId");
                        if (pid != null) idsUnicos.add(pid);
                    }
                    buscarNomesPacientes(new ArrayList<>(idsUnicos));
                });
    }

    private void buscarNomesPacientes(List<String> ids) {
        if (ids.isEmpty()) return;
        pacientesNomes.clear();
        for (String id : ids) {
            db.collection("pacientes").document(id).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String nome = doc.getString("nome");
                            if (nome != null) pacientesNomes.add(nome);
                            adapter.notifyDataSetChanged();
                        }
                    });
        }
    }
}