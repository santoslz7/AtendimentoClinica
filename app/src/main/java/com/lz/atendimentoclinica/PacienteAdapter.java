package com.lz.atendimentoclinica;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PacienteAdapter extends RecyclerView.Adapter<PacienteAdapter.ViewHolder> {

    private final List<PacienteConsulta> lista;

    public PacienteAdapter(List<PacienteConsulta> lista) {
        this.lista = lista;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_paciente, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PacienteConsulta p = lista.get(position);

        holder.tvNome.setText("👤 " + (p.nomePaciente != null ? p.nomePaciente : "-"));
        holder.tvMedico.setText("👨‍⚕️ " + (p.nomeMedico != null ? p.nomeMedico : "-"));
        holder.tvEspecialidade.setText("🏥 " + (p.especialidade != null ? p.especialidade : "-"));

        String data    = p.data    != null ? p.data    : "-";
        String horario = p.horario != null ? p.horario : "-";
        holder.tvDataHora.setText("📅 " + data + "  ⏰ " + horario);

        String status = p.status != null ? p.status : "-";
        holder.tvStatus.setText(status);

        int cor;
        switch (status) {
            case "confirmada":
            case "Confirmada": cor = Color.parseColor("#2E7D32"); break;
            case "cancelada":
            case "Cancelada":  cor = Color.parseColor("#C62828"); break;
            default:           cor = Color.parseColor("#F57C00"); break;
        }
        holder.tvStatus.setTextColor(cor);
    }

    @Override
    public int getItemCount() { return lista.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNome, tvMedico, tvEspecialidade, tvDataHora, tvStatus;

        ViewHolder(View view) {
            super(view);
            tvNome         = view.findViewById(R.id.tvNomePaciente);
            tvMedico       = view.findViewById(R.id.tvMedicoPaciente);
            tvEspecialidade = view.findViewById(R.id.tvEspecialidadePaciente);
            tvDataHora     = view.findViewById(R.id.tvDataHoraPaciente);
            tvStatus       = view.findViewById(R.id.tvStatusPaciente);
        }
    }
}