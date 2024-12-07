package com.example.library;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gamelibery.R;
import com.example.library.service.UserService;
import com.example.library.model.rest.Maestro;

public class Cotizar extends AppCompatActivity {

    private TextView txtComuna, txtDias, infoMaestro, valorCotizacion;
    private Spinner comuna, dias;
    private Button btnCotizar, btnBiblioteca, btnReserva;
    private ImageView imageViewMaestro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Validar si está logeado
        UserService userService = (UserService) getApplicationContext();
        if (!userService.getUsuario().isLogin()) {
            Toast.makeText(this, "Debes iniciar Sesion para acceder a esta pantalla",
                    Toast.LENGTH_SHORT).show();
            // Redirige a un activity
            Intent intent = new Intent(getBaseContext(), Login.class);
            startActivity(intent);
            finish();
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cotizacion);

        // Inicializar vistas
        imageViewMaestro = findViewById(R.id.imageView4);
        txtComuna = findViewById(R.id.txtcomuna);
        txtDias = findViewById(R.id.txtDias);
        infoMaestro = findViewById(R.id.txtMaestro);
        comuna = findViewById(R.id.spinnerComuna);
        dias = findViewById(R.id.spinnerDias);
        btnCotizar = findViewById(R.id.btnCotiza);
        btnBiblioteca = findViewById(R.id.btnCotizarR);
        btnReserva = findViewById(R.id.btnConfirmar);
        valorCotizacion = findViewById(R.id.txtValor);
        Spinner horas = findViewById(R.id.spinnerHoraDetalle);

        // Adaptador para el spinner de horas
        ArrayAdapter<CharSequence> adapterHoras = ArrayAdapter.createFromResource(
                this, R.array.horas, android.R.layout.simple_spinner_item);
        horas.setAdapter(adapterHoras);

        // Recuperar el objeto 'maestro' desde el Intent
        Maestro maestro = (Maestro) getIntent().getSerializableExtra("maestro");

        // Si no se encuentra, mostrar un mensaje y terminar la actividad
        if (maestro == null) {
            Toast.makeText(this, "No se ha recibido información del maestro.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Mostrar el nombre del maestro si se ha recibido correctamente
        if (maestro != null) {
            infoMaestro.setText('\n' + maestro.getNombre() + '\n' + maestro.getNombreCategoria());

            // Decodificar la imagen almacenada como byte[]
            byte[] imageData = maestro.getImage();
            if (imageData != null && imageData.length > 0) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                imageViewMaestro.setImageBitmap(bitmap);
            } else {
                imageViewMaestro.setImageResource(R.drawable.icono_default);
            }
        }

        // Adaptadores para los spinners de comuna y días
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.comunas, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> adapterDias = ArrayAdapter.createFromResource(
                this, R.array.dias, android.R.layout.simple_spinner_item);

        comuna.setAdapter(adapter);
        dias.setAdapter(adapterDias);

        // Eventos para obtener la selección del usuario
        comuna.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                txtComuna.setText(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        dias.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                txtDias.setText(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        btnCotizar.setOnClickListener(view -> cotizar());

        btnBiblioteca.setOnClickListener(view -> {
            Intent intent = new Intent(getBaseContext(), BibliotecaMaestro.class);
            startActivity(intent);
            finish();
        });

        btnReserva.setOnClickListener(view -> {
            // Obtener datos para realizar la reserva
            String comunaSeleccionada = comuna.getSelectedItem() != null ? comuna.getSelectedItem().toString() : "";
            String diaSeleccionado = dias.getSelectedItem() != null ? dias.getSelectedItem().toString() : "";
            String horaSeleccionada = horas.getSelectedItem() != null ? horas.getSelectedItem().toString() : "";
            double valorCotizado = generarPrecioPorComuna(comunaSeleccionada);

            // Enviar los datos al activity de Reserva
            Intent intent = new Intent(getBaseContext(), Reserva.class);
            intent.putExtra("comuna", comunaSeleccionada);
            intent.putExtra("dia", diaSeleccionado);
            intent.putExtra("hora", horaSeleccionada);  // Pasa la hora seleccionada
            intent.putExtra("valor_cotizacion", valorCotizado);
            intent.putExtra("maestro", maestro);  // Pasar el objeto maestro
            startActivity(intent);
            finish();
        });
    }

    // Función para calcular el precio basado en la comuna
    private double generarPrecioPorComuna(String comuna) {
        double precioBase;
        switch (comuna) {
            case "Cerrillos":
            case "Cerro Navia":
            case "Conchalí":
            case "El Bosque":
            case "Lo Espejo":
                precioBase = 20000 + (int)(Math.random() * 5000); // Comunas más económicas
                break;
            case "Las Condes":
            case "Vitacura":
            case "Lo Barnechea":
                precioBase = 45000 + (int)(Math.random() * 5000); // Comunas más caras
                break;
            default:
                precioBase = 30000 + (int)(Math.random() * 10000); // Comunas promedio
                break;
        }

        return precioBase;
    }

    // Función para calcular y mostrar el precio final
    private void cotizar() {
        String comunaSeleccionada = comuna.getSelectedItem() != null ? comuna.getSelectedItem().toString() : "";
        String diaSeleccionado = dias.getSelectedItem() != null ? dias.getSelectedItem().toString() : "";

        // Validar que no se haya seleccionado "Seleccione Comuna" y "Seleccione Día"
        if (comunaSeleccionada.isEmpty()) {
            Toast.makeText(Cotizar.this, "Debe seleccionar una comuna válida", Toast.LENGTH_SHORT).show();
            return;
        }
        if (diaSeleccionado.isEmpty()) {
            Toast.makeText(Cotizar.this, "Debe seleccionar un día válido", Toast.LENGTH_SHORT).show();
            return;
        } else {
            // Generar un valor base de la comuna
            double precioBase = generarPrecioPorComuna(comunaSeleccionada);

            // Aumentar el precio si es fin de semana (sábado o domingo)
            if (diaSeleccionado.equalsIgnoreCase("Sábado") || diaSeleccionado.equalsIgnoreCase("Domingo")) {
                precioBase += 15000; // Incremento adicional por ser fin de semana
            }

            // Limitar el precio final al rango entre $20,000 y $65,000
            if (precioBase > 65000) {
                precioBase = 65000;
            } else if (precioBase < 20000) {
                precioBase = 20000;
            }

            // Mostrar el precio final en el TextView
            valorCotizacion.setText("El precio final es: $" + precioBase);
        }
    }
}
