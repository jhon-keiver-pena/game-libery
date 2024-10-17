package com.example.library;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gamelibery.R;

public class Reserva extends AppCompatActivity {

    private TextView infoReserva;
    private Button volverCotizar, confirmar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reserva);

        infoReserva.findViewById(R.id.txt_Info);
        volverCotizar.findViewById(R.id.btnCotizarR);
        confirmar.findViewById(R.id.btnConfirmar);

    }
}