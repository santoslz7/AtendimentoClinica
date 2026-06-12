package com.lz.atendimentoclinica;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class MedicosFragment extends Fragment {

    private MedicoAdapter adapter;
    private final List<Medico> medicos = new ArrayList<>();
    private FirebaseFirestore db;
    private String clinicaId;
    private TextView tvVazio;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_medicos, container, false);
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

        adapter = new MedicoAdapter(medicos, this::excluirMedico);
        recycler.setAdapter(adapter);

        FloatingActionButton fab = view.findViewById(R.id.fabAdicionarMedico);
        fab.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), CadastroMedicoActivity.class)));

        carregarMedicos();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recarrega ao voltar do CadastroMedicoActivity
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
                    tvVazio.setVisibility(medicos.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }

    private void excluirMedico(String medicoId) {
        if (clinicaId == null || medicoId == null) return;

        db.collection("clinicas").document(clinicaId)
                .collection("medicos").document(medicoId).delete()
                .addOnSuccessListener(v ->
                        Toast.makeText(requireContext(), "Médico removido", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Erro ao remover: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}