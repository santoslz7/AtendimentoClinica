package com.lz.atendimentoclinica;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
// Importação automática do Binding do seu layout
import com.lz.atendimentoclinica.databinding.ActivityDashboardBinding;

public class DashboardActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ActivityDashboardBinding binding; // Objeto que guarda todos os IDs da tela

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configuração correta do View Binding para inflar a tela
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // 1. Configura a Bottom Navigation usando o Container correto
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (binding.bottomNav != null && navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(binding.bottomNav, navController);
        }

        // 2. Configura o botão de Sair através do binding
        if (binding.btnLogout != null) {
            binding.btnLogout.setOnClickListener(v -> {
                mAuth.signOut();
                Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            });
        }

        // 3. Executa a busca para substituir o texto "Carregando..." pelo nome real
        buscarNomeDoUsuarioLogado();
    }

    private void buscarNomeDoUsuarioLogado() {
        if (mAuth.getCurrentUser() == null || binding.tvNomeClinica == null) return;

        String uid = mAuth.getCurrentUser().getUid();

        db.collection("clinicas").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nome = documentSnapshot.getString("nome");
                        if (nome != null && !nome.isEmpty()) {
                            binding.tvNomeClinica.setText(nome);
                        } else {
                            binding.tvNomeClinica.setText("Atendimento");
                        }
                    } else {
                        buscarNomeNaColecaoPacientes(uid);
                    }
                })
                .addOnFailureListener(e -> binding.tvNomeClinica.setText("Atendimento"));
    }

    private void buscarNomeNaColecaoPacientes(String uid) {
        db.collection("pacientes").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists() && doc.getString("nome") != null) {
                        binding.tvNomeClinica.setText(doc.getString("nome"));
                    } else {
                        binding.tvNomeClinica.setText("Atendimento");
                    }
                })
                .addOnFailureListener(e -> binding.tvNomeClinica.setText("Atendimento"));
    }
}