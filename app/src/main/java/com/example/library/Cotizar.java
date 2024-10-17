package com.example.library;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gamelibery.R;
import com.example.library.service.UserService;

public class Cotizar extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Validar si esta logeado
        UserService userService = (UserService) getApplicationContext();
        if (!userService.getUsuario().isLogin()){
            Toast.makeText(this, "Debes iniciar Sesion para acceder a esta pantalla",
                    Toast.LENGTH_SHORT).show();
            //redirige a un activity
            Intent intent = new Intent(getBaseContext(), Login.class);
            startActivity(intent);
            finish();
        }
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cotizacion);
    }
}