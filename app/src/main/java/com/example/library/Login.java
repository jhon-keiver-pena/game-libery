package com.example.library;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gamelibery.R;
import com.example.library.model.rest.Usuario;
import com.example.library.service.UsuarioService;

public class Login extends AppCompatActivity {

    private EditText usuario;
    private EditText contraseña;
    private Button buttonini;
    private Button buttonregis;
    private String email;
    private String contras;
    Usuario adm = new Usuario();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        usuario = findViewById(R.id.input_mail);
        contraseña = findViewById(R.id.input_password);
        buttonini = findViewById(R.id.btn_inicio);
        buttonregis = findViewById(R.id.btn_crear);
        email = adm.getCorreo();
        contras = adm.getClave();

        buttonini.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String username = usuario.getText().toString();
                String password = contraseña.getText().toString();


                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {

                    Toast.makeText(getBaseContext(), "Por favor ingrese usuario y contraseña ", Toast.LENGTH_SHORT).show();
                } else {

                    if ( username.equals(email) || password.equals(contras)) {
                        Intent intent = new Intent(getBaseContext(), Home.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(getBaseContext(), "Usuario o contraseña incorrectos ", Toast.LENGTH_SHORT).show();
                    }
                }

            }


        });

        buttonregis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), DetalleMaestro.class);
                startActivity(intent);
                finish();
            }
        });


    }
}