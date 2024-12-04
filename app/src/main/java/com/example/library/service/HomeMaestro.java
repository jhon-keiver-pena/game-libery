package com.example.library.service;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.gamelibery.R;
import com.example.library.CustomSpinnerAdapter;
import com.example.library.EditarMaestro;
import com.example.library.EditarUsuario;
import com.example.library.Home;
import com.example.library.Login;

import java.util.ArrayList;
import java.util.List;

public class HomeMaestro extends AppCompatActivity {

    private Spinner customSpinner;
    private ViewFlipper vf;

    private int[] image = {R.drawable.img_herramientas, R.drawable.img_obra, R.drawable.img_casco};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Validar si el usuario está logeado
        UserService userService = (UserService) getApplicationContext();
        if (!userService.getUsuario().isLogin()) {
            Toast.makeText(this, "Debes iniciar sesión para acceder a esta pantalla", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, Login.class);
            startActivity(intent);
            finish();
        }

        setContentView(R.layout.activity_home_maestro);

        // Inicializar componentes
        customSpinner = findViewById(R.id.spinner4);
        vf = findViewById(R.id.slider2);

        // Configurar el ViewFlipper
        for (int i = 0; i < image.length; i++) {
            flipImg(image[i]);
        }

        // Configurar el Spinner personalizado
        setupSpinner();
    }

    private void setupSpinner() {
        // Crear lista de opciones con íconos y texto
        List<CustomSpinnerAdapter.SpinnerItem> spinnerItems = new ArrayList<>();
        spinnerItems.add(new CustomSpinnerAdapter.SpinnerItem("Inicio", R.drawable.home));
        spinnerItems.add(new CustomSpinnerAdapter.SpinnerItem("Configuración", R.drawable.cog));
        //spinnerItems.add(new CustomSpinnerAdapter.SpinnerItem("Menú", R.drawable.menu_icon));

        // Adaptador personalizado
        CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(this, spinnerItems);
        customSpinner.setAdapter(adapter);

        // Configurar acción al seleccionar un elemento
        customSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:

                        break;
                    case 1: // Configuración
                        Intent intentConfig = new Intent(HomeMaestro.this, EditarMaestro.class);
                        startActivity(intentConfig);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hacer nada
            }
        });
    }

    public void flipImg(int i) {
        ImageView imageView = new ImageView(this);
        imageView.setBackgroundResource(i);
        vf.addView(imageView);
        vf.setFlipInterval(2800);
        vf.setAutoStart(true);
        vf.setInAnimation(this, android.R.anim.slide_in_left);
        vf.setOutAnimation(this, android.R.anim.slide_out_right);
    }
}