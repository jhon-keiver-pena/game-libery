package com.example.gamelibery;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public abstract class MainActivity extends AppCompatActivity {

    //declaracion de variable
    private Button inicio;
    private Button registrar;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
    }

    //lamada por id
    inicio = findViewById(R.id.button1);
    registrar = findViewById(R.id.button2);

}