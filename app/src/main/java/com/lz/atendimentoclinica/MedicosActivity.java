package com.lz.atendimentoclinica;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.List;

public class MedicosActivity extends AppCompatActivity {

    private MedicoAdapter adapter;
    private final List<Medico> medicos = new ArrayList<>();
    private FirebaseFirestore db;
    private String clinicaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicos);

        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Médicos");

        db = FirebaseFirestore.getInstance();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            clinicaId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        RecyclerView recycler = findViewById(R.id.recyclerView);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new MedicoAdapter(medicos, this::excluirMedico);
        recycler.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAdicionarMedico);
        fab.setOnClickListener(v ->
                startActivity(new Intent(this, CadastroMedicoActivity.class)));

        carregarMedicos();
    }

    private void carregarMedicos() {
        if (clinicaId == null) return;

        db.collection("clinicas").document(clinicaId).collection("medicos")
                .addSnapshotListener((snap, e) -> {
                    if (e != null || snap == null) return;
                    medicos.clear();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        Medico m = doc.toObject(Medico.class);
                        if (m != null) {
                            m.setId(doc.getId());
                            medicos.add(m);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }
    private void carregarMedico() {
        if (clinicaId == null) {
            Log.e("MEDICOS", "clinicaId é null!");
            return;
        }

        Log.d("MEDICOS", "Buscando médicos para clinicaId: " + clinicaId);

        db.collection("clinicas").document(clinicaId).collection("medicos")
                .addSnapshotListener((snap, e) -> {
                    if (e != null) {
                        Log.e("MEDICOS", "Erro Firestore: " + e.getMessage());
                        return;
                    }
                    if (snap == null) {
                        Log.e("MEDICOS", "snap é null");
                        return;
                    }

                    Log.d("MEDICOS", "Documentos encontrados: " + snap.size());

                    medicos.clear();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        Medico m = doc.toObject(Medico.class);
                        Log.d("MEDICOS", "Médico: " + (m != null ? m.getNome() : "null"));
                        if (m != null) {
                            m.setId(doc.getId());
                            medicos.add(m);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }
    private void excluirMedico(String medicoId) {
        if (clinicaId == null || medicoId == null) return;

        db.collection("clinicas").document(clinicaId)
                .collection("medicos").document(medicoId).delete()
                .addOnSuccessListener(v ->
                        Toast.makeText(this, "Médico removido", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao remover: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}