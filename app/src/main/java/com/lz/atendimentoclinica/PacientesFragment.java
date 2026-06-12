package com.lz.atendimentoclinica;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PacientesFragment extends Fragment {

    private PacienteAdapter adapter;
    private final List<PacienteConsulta> consultas = new ArrayList<>();
    private FirebaseFirestore db;
    private String clinicaId;
    private TextView tvVazio;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pacientes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            clinicaId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        tvVazio = view.findViewById(R.id.tvVazio);

        RecyclerView recycler = view.findViewById(R.id.recyclerView);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new PacienteAdapter(consultas);
        recycler.setAdapter(adapter);

        carregarPacientes();
    }

    private void carregarPacientes() {
        if (clinicaId == null) return;

        db.collection("consultas")
                .whereEqualTo("clinicaId", clinicaId)
                .get()
                .addOnSuccessListener(snap -> {
                    consultas.clear();
                    List<DocumentSnapshot> docs = snap.getDocuments();

                    if (docs.isEmpty()) {
                        tvVazio.setVisibility(View.VISIBLE);
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    tvVazio.setVisibility(View.GONE);
                    AtomicInteger pendentes = new AtomicInteger(docs.size());

                    for (DocumentSnapshot doc : docs) {
                        String nomePaciente  = doc.getString("nomePaciente");
                        String nomeMedico    = doc.getString("nomeMedico");
                        String especialidade = doc.getString("especialidade");
                        String data          = doc.getString("data");
                        String horario       = doc.getString("horario");
                        String status        = doc.getString("status");
                        String pacienteId    = doc.getString("pacienteId");

                        // Se o nome do paciente já está na consulta, usa direto
                        if (nomePaciente != null && !nomePaciente.isEmpty()) {
                            consultas.add(new PacienteConsulta(
                                    nomePaciente, nomeMedico, especialidade, data, horario, status));
                            if (pendentes.decrementAndGet() == 0) adapter.notifyDataSetChanged();
                        } else if (pacienteId != null) {
                            // Busca o nome do paciente pelo ID
                            final String nMedico = nomeMedico;
                            final String esp     = especialidade;
                            final String dt      = data;
                            final String hr      = horario;
                            final String st      = status;

                            db.collection("pacientes").document(pacienteId).get()
                                    .addOnSuccessListener(pacDoc -> {
                                        String nome = pacDoc.exists()
                                                ? pacDoc.getString("nome") : "Desconhecido";
                                        consultas.add(new PacienteConsulta(
                                                nome, nMedico, esp, dt, hr, st));
                                        if (pendentes.decrementAndGet() == 0)
                                            adapter.notifyDataSetChanged();
                                    })
                                    .addOnFailureListener(e -> {
                                        consultas.add(new PacienteConsulta(
                                                "Desconhecido", nMedico, esp, dt, hr, st));
                                        if (pendentes.decrementAndGet() == 0)
                                            adapter.notifyDataSetChanged();
                                    });
                        } else {
                            if (pendentes.decrementAndGet() == 0) adapter.notifyDataSetChanged();
                        }
                    }
                });
    }
}