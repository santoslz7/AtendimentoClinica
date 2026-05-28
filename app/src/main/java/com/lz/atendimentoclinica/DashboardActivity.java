package com.lz.atendimentoclinica;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.lz.atendimentoclinica.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        TextView tvNome = findViewById(R.id.tvNomeClinica);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance().collection("clinicas").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) tvNome.setText(doc.getString("nome"));
                });

        findViewById(R.id.cardAgendamentos).setOnClickListener(v ->
                startActivity(new Intent(this, AgendamentosActivity.class)));

        findViewById(R.id.cardMedicos).setOnClickListener(v ->
                startActivity(new Intent(this, MedicosActivity.class)));

        findViewById(R.id.cardPacientes).setOnClickListener(v ->
                startActivity(new Intent(this, PacientesActivity.class)));

        findViewById(R.id.cardHorarios).setOnClickListener(v ->
                startActivity(new Intent(this, HorariosActivity.class)));

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finishAffinity();
        });
    }
}