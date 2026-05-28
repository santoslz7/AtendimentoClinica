package com.lz.atendimentoclinica;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lz.atendimentoclinica.R;

import java.util.List;

public class MedicoAdapter extends RecyclerView.Adapter<MedicoAdapter.ViewHolder> {

    public interface OnExcluirListener { void onExcluir(String medicoId); }

    private final List<Medico> lista;
    private final OnExcluirListener listener;

    public MedicoAdapter(List<Medico> lista, OnExcluirListener listener) {
        this.lista = lista;
        this.listener = listener;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medico, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Medico m = lista.get(position);
        holder.tvNome.setText(m.getNome());
        holder.tvEspecialidade.setText(m.getEspecialidade());
        holder.tvCrm.setText("CRM: " + m.getCrm());
        holder.btnExcluir.setOnClickListener(v -> listener.onExcluir(m.getId()));
    }
    @Override
    public int getItemCount() { return lista.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNome, tvEspecialidade, tvCrm;
        Button btnExcluir;
        ViewHolder(View view) {
            super(view);
            tvNome          = view.findViewById(R.id.tvNomeMedico);
            tvEspecialidade = view.findViewById(R.id.tvEspecialidade);
            tvCrm           = view.findViewById(R.id.tvCrm);
            btnExcluir      = view.findViewById(R.id.btnExcluir);
        }
    }
}