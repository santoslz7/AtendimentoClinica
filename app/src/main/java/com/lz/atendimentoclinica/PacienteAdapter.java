package com.lz.atendimentoclinica;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PacienteAdapter extends RecyclerView.Adapter<PacienteAdapter.ViewHolder> {

    private final List<String> nomes;

    public PacienteAdapter(List<String> nomes) { this.nomes = nomes; }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvNome.setText("👤  " + nomes.get(position));
    }

    @Override
    public int getItemCount() { return nomes.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNome;
        ViewHolder(View view) {
            super(view);
            tvNome = view.findViewById(android.R.id.text1);
        }
    }
}