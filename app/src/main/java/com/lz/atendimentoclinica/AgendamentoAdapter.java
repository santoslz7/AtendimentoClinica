package com.lz.atendimentoclinica;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
public class AgendamentoAdapter extends RecyclerView.Adapter<AgendamentoAdapter.ViewHolder> {

    private final List<Consulta> lista;
    private final OnAcaoListener listener;

    public interface OnAcaoListener {
        void onConfirmar(String id);
        void onCancelar(String id);
    }

    public AgendamentoAdapter(List<Consulta> lista, OnAcaoListener listener) {
        this.lista = lista;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_agendamento, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Consulta c = lista.get(position);

        holder.tvNomePaciente.setText("Paciente: " +
                (c.getNomePaciente() != null ? c.getNomePaciente() : "-"));

        holder.tvMedico.setText("Médico: " +
                (c.getNomeMedico() != null ? c.getNomeMedico() : "-") +
                " - " +
                (c.getEspecialidade() != null ? c.getEspecialidade() : ""));

        // CORRIGIDO: usa getHorario() em vez de getHora()
        String data    = c.getData()    != null ? c.getData()    : "-";
        String horario = c.getHorario() != null ? c.getHorario() : "-";
        holder.tvDataHora.setText("📅 " + data + "  ⏰ " + horario);

        holder.tvStatus.setText(c.getStatus() != null ? c.getStatus() : "-");

        holder.btnConfirmar.setOnClickListener(v -> {
            if (listener != null && c.getId() != null) listener.onConfirmar(c.getId());
        });

        holder.btnCancelar.setOnClickListener(v -> {
            if (listener != null && c.getId() != null) listener.onCancelar(c.getId());
        });
    }

    @Override
    public int getItemCount() { return lista.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNomePaciente, tvMedico, tvDataHora, tvStatus;
        Button btnConfirmar, btnCancelar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNomePaciente = itemView.findViewById(R.id.tvNomePaciente);
            tvMedico       = itemView.findViewById(R.id.tvMedico);
            tvDataHora     = itemView.findViewById(R.id.tvDataHora);
            tvStatus       = itemView.findViewById(R.id.tvStatus);
            btnConfirmar   = itemView.findViewById(R.id.btnConfirmar);
            btnCancelar    = itemView.findViewById(R.id.btnCancelar);
        }
    }
}