package com.example.library;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gamelibery.R;
import com.example.library.service.UserService;

import java.util.ArrayList;
import java.util.List;

public class Home extends AppCompatActivity {

    private Spinner customSpinner;
    private ViewFlipper vf;
    private Button btnCrear, btnReserva;

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

        setContentView(R.layout.activity_home);

        // Inicializar componentes
        customSpinner = findViewById(R.id.spinner3);
        vf = findViewById(R.id.slider2);
        btnCrear = findViewById(R.id.ir_biblioteca);
        btnReserva = findViewById(R.id.btn_reservas);

        // Configurar el ViewFlipper
        for (int i = 0; i < image.length; i++) {
            flipImg(image[i]);
        }

        // Configurar el Spinner personalizado
        setupSpinner();

        // Acciones de botones
        btnCrear.setOnClickListener(view -> {
            Intent intent = new Intent(Home.this, BibliotecaMaestro.class);
            startActivity(intent);
            finish();
        });

        btnReserva.setOnClickListener(view -> {
            Intent intent = new Intent(Home.this, ListaReservaActivity.class);
            startActivity(intent);
            finish();
        });
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
                        Intent intentConfig = new Intent(Home.this, EditarUsuario.class);
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
