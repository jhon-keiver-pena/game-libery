package com.example.library;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.gamelibery.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DetalleReserva extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_reserva);

        // Recibir el ID de la reserva desde la actividad anterior
        int idReserva = getIntent().getIntExtra("idReserva", -1);

        // Verificar si se recibió un ID válido
        if (idReserva == -1) {
            Toast.makeText(this, "Error: No se recibió el ID de la reserva", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Vincular los elementos de la interfaz
        TextView infoMaestro = findViewById(R.id.info_maestro);
        EditText infoDia = findViewById(R.id.info_dia);
        Spinner spinnerCiudad = findViewById(R.id.spinner_info_ciudad);
        Spinner spinnerEstado = findViewById(R.id.spinner_info_estado);

        // Configurar los valores de los spinners
        configurarSpinners(spinnerCiudad, spinnerEstado);

        // Obtener los detalles de la reserva
        obtenerDetalleReserva(idReserva, infoMaestro, infoDia, spinnerCiudad, spinnerEstado);

        // Configurar los botones
        Button btnActualizar = findViewById(R.id.btn_actualizar);
        Button btnVolver = findViewById(R.id.btn_volver);
        Button btnHome = findViewById(R.id.btn_home);

        btnActualizar.setOnClickListener(v -> actualizarReserva(idReserva,
                infoDia.getText().toString(),
                spinnerCiudad.getSelectedItem().toString(),
                spinnerEstado.getSelectedItemPosition() + 1));

        btnVolver.setOnClickListener(v -> finish());
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(DetalleReserva.this, Home.class);
            startActivity(intent);
            finish();
        });
    }

    private void obtenerDetalleReserva(int idReserva, TextView infoMaestro, EditText infoDia, Spinner spinnerCiudad, Spinner spinnerEstado) {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            try {
                // Construir la URL
                URL url = new URL("https://ms-reserva-1078682117753.us-central1.run.app/v1/reservas/" + idReserva);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Content-Type", "application/json");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    StringBuilder responseBuilder = new StringBuilder();
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                        String inputLine;
                        while ((inputLine = in.readLine()) != null) {
                            responseBuilder.append(inputLine);
                        }
                    }

                    JSONObject jsonReserva = new JSONObject(responseBuilder.toString());
                    String maestro = jsonReserva.getString("id_maestro");
                    String fechaVisita = jsonReserva.getString("fecha_visita");
                    String ciudad = jsonReserva.getString("ciudad");
                    int estado = jsonReserva.getInt("id_estado");

                    runOnUiThread(() -> {
                        infoMaestro.setText(maestro);
                        infoDia.setText(fechaVisita);

                        // Seleccionar la ciudad en el spinner
                        ArrayAdapter<CharSequence> ciudadAdapter = (ArrayAdapter<CharSequence>) spinnerCiudad.getAdapter();
                        int ciudadPosition = ciudadAdapter.getPosition(ciudad);
                        spinnerCiudad.setSelection(ciudadPosition);

                        // Seleccionar el estado en el spinner
                        spinnerEstado.setSelection(estado == 1 ? 0 : 1); // 1 = Activa, 2 = Cancelada
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Error al obtener la reserva: " + responseCode, Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void actualizarReserva(int idReserva, String nuevaFecha, String nuevaCiudad, int nuevoEstado) {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            try {
                // Construir la URL
                URL url = new URL("https://ms-reserva-1078682117753.us-central1.run.app/v1/reservas/" + idReserva);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                // Configurar la conexión
                connection.setRequestMethod("PUT");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                // Crear el cuerpo de la solicitud
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("fecha_visita", nuevaFecha);
                jsonBody.put("ciudad", nuevaCiudad);
                jsonBody.put("id_estado", nuevoEstado);

                // Enviar la solicitud
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonBody.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    runOnUiThread(() -> Toast.makeText(this, "Reserva actualizada con éxito", Toast.LENGTH_SHORT).show());
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Error al actualizar: " + responseCode, Toast.LENGTH_SHORT).show());
                }

                connection.disconnect();
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void borrarReserva(int idReserva) {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            try {
                // Construir la URL
                URL url = new URL("https://ms-reserva-1078682117753.us-central1.run.app/v1/reservas/" + idReserva);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("DELETE");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Reserva eliminada con éxito", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Error al eliminar: " + responseCode, Toast.LENGTH_SHORT).show());
                }

                connection.disconnect();
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    // Método para configurar los spinners de ciudad y estado
    private void configurarSpinners(Spinner spinnerCiudad, Spinner spinnerEstado) {
        // Crear adaptadores para los spinners
        ArrayAdapter<CharSequence> adapterCiudad = ArrayAdapter.createFromResource(
                this, R.array.comunas, android.R.layout.simple_spinner_item);
        adapterCiudad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<CharSequence> adapterEstado = ArrayAdapter.createFromResource(
                this, R.array.estados, android.R.layout.simple_spinner_item);
        adapterEstado.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Configurar los spinners
        spinnerCiudad.setAdapter(adapterCiudad);
        spinnerEstado.setAdapter(adapterEstado);
    }
}


