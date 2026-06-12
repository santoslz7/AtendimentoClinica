package com.lz.atendimentoclinica;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView; // Importante
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HorariosFragment extends Fragment {

    private Spinner spinnerMedico, spinnerDia;
    private TextView tvEspecialidadeMedico;
    private TextInputEditText etHoraInicio, etHoraFim;
    private FirebaseFirestore db;
    private String clinicaId;

    private final Map<String, DadosMedico> medicosMap = new HashMap<>();

    private static class DadosMedico {
        String id;
        String especialidade;
        DadosMedico(String id, String especialidade) {
            this.id = id;
            this.especialidade = especialidade;
        }
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_horarios, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            clinicaId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        spinnerMedico          = view.findViewById(R.id.spinnerMedico);
        spinnerDia             = view.findViewById(R.id.spinnerDia);
        tvEspecialidadeMedico  = view.findViewById(R.id.tvEspecialidadeMedico); // Inicializado
        etHoraInicio           = view.findViewById(R.id.etHoraInicio);
        etHoraFim              = view.findViewById(R.id.etHoraFim);

        Button btnSalvar = view.findViewById(R.id.btnSalvarHorario);
        btnSalvar.setOnClickListener(v -> salvarHorario());

        configurarSpinnerDiasDinamicos();
        carregarMedicos();

        spinnerMedico.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String nomeSelecionado = spinnerMedico.getSelectedItem().toString();
                DadosMedico dados = medicosMap.get(nomeSelecionado);

                if (dados != null && dados.especialidade != null) {
                    tvEspecialidadeMedico.setText("Especialidade: " + dados.especialidade);
                } else {
                    tvEspecialidadeMedico.setText("Especialidade: Não informada");
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                tvEspecialidadeMedico.setText("Especialidade: ---");
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        carregarMedicos();
    }

    private void configurarSpinnerDiasDinamicos() {
        List<String> diasDinamicos = new ArrayList<>();
        SimpleDateFormat formatoExibicao = new SimpleDateFormat("EEEE '( 'dd/MM' )'", new Locale("pt", "BR"));
        Calendar calendar = Calendar.getInstance();

        for (int i = 0; i < 7; i++) {
            String diaFormatado = formatoExibicao.format(calendar.getTime());
            diaFormatado = diaFormatado.substring(0, 1).toUpperCase() + diaFormatado.substring(1);
            diasDinamicos.add(diaFormatado);
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        ArrayAdapter<String> adapterDias = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, diasDinamicos);
        adapterDias.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDia.setAdapter(adapterDias);
    }

    private void carregarMedicos() {
        if (clinicaId == null) return;

        db.collection("clinicas").document(clinicaId).collection("medicos").get()
                .addOnSuccessListener(snap -> {
                    medicosMap.clear();
                    String[] nomes = new String[snap.size()];
                    int i = 0;
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        String nome = doc.getString("nome");
                        String espec = doc.getString("especialidade"); // Busca a especialidade do Firestore

                        if (nome != null) {
                            medicosMap.put(nome, new DadosMedico(doc.getId(), espec));
                            nomes[i++] = nome;
                        }
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                            android.R.layout.simple_spinner_item, nomes);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerMedico.setAdapter(adapter);
                });
    }

    private void salvarHorario() {
        if (spinnerMedico.getSelectedItem() == null) {
            Toast.makeText(requireContext(), "Nenhum médico cadastrado", Toast.LENGTH_SHORT).show();
            return;
        }

        String medicoNome = spinnerMedico.getSelectedItem().toString();
        DadosMedico dados = medicosMap.get(medicoNome);

        if (dados == null) return;

        String medicoId   = dados.id;
        String dia        = spinnerDia.getSelectedItem().toString();
        String horaInicio = etHoraInicio.getText().toString().trim();
        String horaFim    = etHoraFim.getText().toString().trim();

        if (horaInicio.isEmpty() || horaFim.isEmpty()) {
            Toast.makeText(requireContext(), "Preencha os horários", Toast.LENGTH_SHORT).show();
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
                .addOnSuccessListener(r -> {
                    Toast.makeText(requireContext(), "Horário salvo!", Toast.LENGTH_SHORT).show();
                    etHoraInicio.setText("");
                    etHoraFim.setText("");
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Erro ao salvar", Toast.LENGTH_SHORT).show());

    }

}