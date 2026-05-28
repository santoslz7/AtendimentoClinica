package com.lz.atendimentoclinica;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AgendamentosActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private AgendamentoAdapter adapter;
    private final List<Consulta> consultas = new ArrayList<>();
    private String clinicaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_generica);

        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Agendamentos");

        db = FirebaseFirestore.getInstance();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            clinicaId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        RecyclerView recycler = findViewById(R.id.recyclerView);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AgendamentoAdapter(consultas, new AgendamentoAdapter.OnAcaoListener() {
            @Override
            public void onConfirmar(String id) { atualizarStatus(id, "confirmada"); }

            @Override
            public void onCancelar(String id) { atualizarStatus(id, "cancelada"); }
        });

        recycler.setAdapter(adapter);
        carregarAgendamentos();
    }

    private void carregarAgendamentos() {
        if (clinicaId == null) return;

        db.collection("consultas")
                .whereEqualTo("clinicaId", clinicaId)
                .get()
                .addOnSuccessListener(snap -> {
                    List<DocumentSnapshot> docs = snap.getDocuments();
                    if (docs.isEmpty()) {
                        consultas.clear();
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    consultas.clear();
                    AtomicInteger pendentes = new AtomicInteger(docs.size());

                    for (DocumentSnapshot doc : docs) {
                        Consulta c = doc.toObject(Consulta.class);
                        if (c == null) {
                            if (pendentes.decrementAndGet() == 0) adapter.notifyDataSetChanged();
                            continue;
                        }
                        c.setId(doc.getId());

                        // Se nomePaciente ou nomeMedico estiverem vazios, busca pelo ID
                        boolean temNomePaciente = c.getNomePaciente() != null && !c.getNomePaciente().isEmpty();
                        boolean temNomeMedico   = c.getNomeMedico()   != null && !c.getNomeMedico().isEmpty();

                        if (temNomePaciente && temNomeMedico) {
                            consultas.add(c);
                            if (pendentes.decrementAndGet() == 0) adapter.notifyDataSetChanged();
                        } else {
                            // Busca nome do paciente se necessário
                            preencherNomes(c, temNomePaciente, temNomeMedico, pendentes);
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao carregar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void preencherNomes(Consulta c, boolean temNomePaciente, boolean temNomeMedico, AtomicInteger pendentes) {
        AtomicInteger buscas = new AtomicInteger(0);
        if (!temNomePaciente && c.getPacienteId() != null) buscas.incrementAndGet();
        if (!temNomeMedico   && c.getMedicoId()   != null) buscas.incrementAndGet();

        if (buscas.get() == 0) {
            consultas.add(c);
            if (pendentes.decrementAndGet() == 0) adapter.notifyDataSetChanged();
            return;
        }

        AtomicInteger concluidas = new AtomicInteger(0);

        if (!temNomePaciente && c.getPacienteId() != null) {
            db.collection("pacientes").document(c.getPacienteId()).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String nome = doc.getString("nome");
                            if (nome != null) c.setNomePaciente(nome);
                        }
                        if (concluidas.incrementAndGet() == buscas.get()) {
                            consultas.add(c);
                            if (pendentes.decrementAndGet() == 0) adapter.notifyDataSetChanged();
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (concluidas.incrementAndGet() == buscas.get()) {
                            consultas.add(c);
                            if (pendentes.decrementAndGet() == 0) adapter.notifyDataSetChanged();
                        }
                    });
        }

        if (!temNomeMedico && c.getMedicoId() != null) {
            db.collection("clinicas").document(clinicaId)
                    .collection("medicos").document(c.getMedicoId()).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String nome = doc.getString("nome");
                            String esp  = doc.getString("especialidade");
                            if (nome != null) c.setNomeMedico(nome);
                            if (esp  != null) c.setEspecialidade(esp);
                        }
                        if (concluidas.incrementAndGet() == buscas.get()) {
                            consultas.add(c);
                            if (pendentes.decrementAndGet() == 0) adapter.notifyDataSetChanged();
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (concluidas.incrementAndGet() == buscas.get()) {
                            consultas.add(c);
                            if (pendentes.decrementAndGet() == 0) adapter.notifyDataSetChanged();
                        }
                    });
        }
    }

    private void atualizarStatus(String id, String novoStatus) {
        if (id == null) return;
        db.collection("consultas").document(id)
                .update("status", novoStatus)
                .addOnSuccessListener(v -> {
                    Toast.makeText(this, "Status: " + novoStatus, Toast.LENGTH_SHORT).show();
                    carregarAgendamentos();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao atualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}