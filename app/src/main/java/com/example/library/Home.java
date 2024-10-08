package com.example.library;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gamelibery.R;

public class Home extends AppCompatActivity {

    private Button btnCrear, btnReserva;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        btnCrear = findViewById(R.id.ir_biblioteca);
        btnReserva = findViewById(R.id.btn_reservas);

        // Acción del botón
        btnCrear.setOnClickListener(view -> {
            //redirige a un activity
            Intent intent = new Intent(getBaseContext(), BibliotecaMaestro.class);
            startActivity(intent);
            finish();
        });

        // Acción del botón
        btnReserva.setOnClickListener(view -> {
            //redirige a un activity
            Intent intent = new Intent(getBaseContext(), ListaReservaActivity.class);
            startActivity(intent);
            finish();
        });

    }
}