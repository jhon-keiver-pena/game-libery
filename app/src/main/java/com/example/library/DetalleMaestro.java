package com.example.library;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.gamelibery.R;
import com.example.library.model.rest.Maestro;

public class DetalleMaestro extends AppCompatActivity {

    private TextView txtNombre, txtEdad, txtSexo, txtAno, txtExperiencia;
    private ImageView imageViewMaestro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_maestro); // Tu XML

        // Obtener las referencias a los TextViews e ImageView
        txtNombre = findViewById(R.id.txtNombre);
        txtEdad = findViewById(R.id.txtEdad);
        txtSexo = findViewById(R.id.textSexo);
        txtAno = findViewById(R.id.txtano);
        txtExperiencia = findViewById(R.id.txtExperiencia);
        imageViewMaestro = findViewById(R.id.imageView4);

        // Obtener el objeto Maestro pasado desde el Intent
        Maestro maestro = (Maestro) getIntent().getSerializableExtra("maestro");

        // Mostrar los valores del objeto Maestro en los TextViews
        if (maestro != null) {
            txtNombre.setText(maestro.getNombre());
            txtEdad.setText(maestro.getEdad());
            txtSexo.setText(maestro.getSexo());
            txtAno.setText(maestro.getTiempoCampo()); // Asumiendo que este es el valor de "Año experiencia"
            txtExperiencia.setText(maestro.getExperiencia());

            // Si tienes una URL de la imagen, puedes cargarla aquí (usando Glide o Picasso)
            // Ejemplo con Glide:
             Glide.with(this).load(maestro.getUrlImagen()).into(imageViewMaestro);
        }
    }
}
