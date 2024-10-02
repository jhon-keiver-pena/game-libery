package com.example.library;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gamelibery.R;


public class MainActivity extends AppCompatActivity {

    //declaracion de variable
    private Button inicio;
    private Button registrar;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        //llamada por id
        inicio = findViewById(R.id.btt_iniciar);
        registrar = findViewById(R.id.btt_registrar);

        //public void onClick(View view) {
            // escribo la logica para iniciar sesion (los if)
            //String username = usuario.getText().toString();
            //String contrasena = MainActivity.this.contrasena.getText().toString();

            //if (TextUtils.isEmpty(username) || TextUtils.isEmpty(contrasena)){
                // toast = notificacion
                Toast.makeText(getBaseContext(),"Por favor ingrese usuario y contraseña", Toast.LENGTH_SHORT).show();
            //} else{
                //if (username.equals(admin.usuario) && contrasena.equals(admin.contrasena)){
                    // abre un activity
                    //Intent intent = new Intent(getBaseContext(), Ficha_act.class);
                    //startActivity(intent);
                    //finish();

                //}else {
                    // toas = notificacion
                    //Toast.makeText(getBaseContext(), "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                //}
            //}
        };




    }
