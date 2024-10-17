package com.example.library;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.gamelibery.R;
import com.example.library.model.rest.Maestro;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public class DetalleMaestro extends AppCompatActivity {

    private TextView txtNombre, txtEdad, txtSexo, txtAno, txtExperiencia;
    private ImageView imageViewMaestro;
    private Button btnCotizar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_maestro); // Tu XML

        // Obtener las referencias a los TextViews e ImageView
        txtNombre = findViewById(R.id.txtNombre);
        txtEdad = findViewById(R.id.txtEdad);
        txtSexo = findViewById(R.id.txtSexo);
        txtAno = findViewById(R.id.txtAno);
        txtExperiencia = findViewById(R.id.txtExperiencia);
        imageViewMaestro = findViewById(R.id.imageView4);
        btnCotizar = findViewById(R.id.btnCotizar);

        // Obtener el objeto Maestro pasado desde el Intent
        Maestro maestro = (Maestro) getIntent().getSerializableExtra("maestro");

        // Mostrar los valores del objeto Maestro en los TextViews
        if (maestro != null) {
            txtNombre.setText(maestro.getNombre());
            txtEdad.setText(maestro.getEdad());
            txtSexo.setText(maestro.getSexo());
            if (maestro.getTiempoCampo() !=null){

            }
            txtAno.setText(calcularFecha(maestro.getTiempoCampo())); // Asumiendo que este es el valor de "Año experiencia"
            txtExperiencia.setText(maestro.getExperiencia());

            // Si tienes una URL de la imagen, puedes cargarla aquí (usando Glide o Picasso)
            // Ejemplo con Glide:
             Glide.with(this).load(maestro.getUrlImagen()).into(imageViewMaestro);
        }

        //funcionalidad de los botones
        btnCotizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //redirige a un activity
                Intent intent = new Intent(getBaseContext(), Cotizar.class);

                // Pasar el objeto Maestro completo
                if (maestro != null) {
                    intent.putExtra("maestro", maestro);  // Pasamos el objeto Maestro
                    intent.putExtra("nombre_maestro", maestro.getNombre());  // Pasamos el nombre del maestro
                }

                startActivity(intent);
                finish();
            }
        });
    }

    public static String calcularFecha(Date fechaInicial) {
        Calendar inicio = Calendar.getInstance();
        inicio.setTime(fechaInicial);
        Calendar actual = Calendar.getInstance();

        // Calcular la diferencia en años y meses
        int años = actual.get(Calendar.YEAR) - inicio.get(Calendar.YEAR);
        int meses = actual.get(Calendar.MONTH) - inicio.get(Calendar.MONTH);

        // Ajuste si el mes de inicio es mayor que el mes actual
        if (meses < 0) {
            años--;
            meses += 12;
        }
        return años + " Años y " + meses + " Meses";
    }

}
