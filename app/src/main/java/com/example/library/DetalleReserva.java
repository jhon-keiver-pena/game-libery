package com.example.library;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gamelibery.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
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
        Spinner spinnerCiudad = findViewById(R.id.spinner_info_ciudad);
        Spinner spinnerEstado = findViewById(R.id.spinner_info_estado);
        Spinner dias = findViewById(R.id.spinnerDiasDetalle);

        ArrayAdapter<CharSequence> adapterDias = ArrayAdapter.createFromResource(
                this, R.array.dias, android.R.layout.simple_spinner_item);
        // Configurar los valores de los spinners
        configurarSpinners(spinnerCiudad, spinnerEstado);

        // Obtener los detalles de la reserva
        obtenerDetalleReserva(idReserva, infoMaestro, spinnerCiudad, spinnerEstado);

        // Configurar los botones
        Button btnActualizar = findViewById(R.id.btn_actualizar);
        Button btnVolver = findViewById(R.id.btn_volver);
        Button btnHome = findViewById(R.id.btn_home);



        Spinner horas = findViewById(R.id.spinnerHoraDetalle);

        // Adaptador para el spinner de horas
        ArrayAdapter<CharSequence> adapterHoras = ArrayAdapter.createFromResource(
                this, R.array.horas, android.R.layout.simple_spinner_item);
        horas.setAdapter(adapterHoras);
        dias.setAdapter(adapterDias);
        String diaSeleccionado = dias.getSelectedItem() != null ? dias.getSelectedItem().toString() : "";
        String horaSeleccionada = horas.getSelectedItem() != null ? horas.getSelectedItem().toString() : "";
        String fechaVisita = obtenerFechaConHora(diaSeleccionado, horaSeleccionada);

        btnActualizar.setOnClickListener(v -> actualizarReserva(idReserva,
                fechaVisita,
                spinnerCiudad.getSelectedItem().toString(),
                spinnerEstado.getSelectedItemPosition() + 1));

        btnVolver.setOnClickListener(v -> finish());
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(DetalleReserva.this, Home.class);
            startActivity(intent);
            finish();
        });
    }

    private void obtenerDetalleReserva(int idReserva, TextView infoMaestro, Spinner spinnerCiudad, Spinner spinnerEstado) {
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
                    String ciudad = jsonReserva.getString("ciudad");
                    int estado = jsonReserva.getInt("id_estado");

                    //Extraer
                    String nombreMaestro = getMaestro(maestro);
                    infoMaestro.setText(nombreMaestro);

                    // Seleccionar la ciudad en el spinner
                    ArrayAdapter<CharSequence> ciudadAdapter = (ArrayAdapter<CharSequence>) spinnerCiudad.getAdapter();
                    int ciudadPosition = ciudadAdapter.getPosition(ciudad);
                    spinnerCiudad.setSelection(ciudadPosition);

                    // Seleccionar el estado en el spinner
                    spinnerEstado.setSelection(estado == 1 ? 0 : 1); // 1 = Activa, 2 = Cancelada

                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Error al obtener la reserva: " + responseCode, Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private String getMaestro(String idMaestro) {
        String nombreMaestro = "Maestro";
        try {
            URL url = new URL("https://ms-maestros-1078682117753.us-central1.run.app/v1/maestros/"+idMaestro);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

            // Configurar la conexión
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setRequestProperty("Content-Type", "application/json");
            httpURLConnection.setDoInput(true);

            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Leer la respuesta
                StringBuilder responseBuilder = new StringBuilder();
                try (BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()))) {
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        responseBuilder.append(inputLine);
                    }
                }

                // Convertir la respuesta en un array JSON
                JSONArray maestrosArray = new JSONArray(responseBuilder.toString());
                for (int i = 0; i < maestrosArray.length(); i++) {
                    JSONObject maestro = maestrosArray.getJSONObject(i);
                    nombreMaestro = maestro.getString("nombre");
                }
            }
        }catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        return nombreMaestro;
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
                    Intent intent = new Intent(getBaseContext(), Home.class);
                    startActivity(intent);
                    finish();
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Error al actualizar: " + responseCode, Toast.LENGTH_SHORT).show());
                }

                connection.disconnect();
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }
    // Función para obtener la fecha y hora combinadas
    private String obtenerFechaConHora(String diaNombre, String horaSeleccionada) {
        int diaDeseado;
        switch (diaNombre.toLowerCase()) {
            case "domingo": diaDeseado = Calendar.SUNDAY; break;
            case "lunes": diaDeseado = Calendar.MONDAY; break;
            case "martes": diaDeseado = Calendar.TUESDAY; break;
            case "miercoles": diaDeseado = Calendar.WEDNESDAY; break;
            case "jueves": diaDeseado = Calendar.THURSDAY; break;
            case "viernes": diaDeseado = Calendar.FRIDAY; break;
            case "sabado": diaDeseado = Calendar.SATURDAY; break;
            default: return null;
        }

        Calendar calendar = Calendar.getInstance();
        int diaActual = calendar.get(Calendar.DAY_OF_WEEK);
        int diasHastaProximo = (diaDeseado - diaActual + 7) % 7;

        if (diasHastaProximo == 0) {
            diasHastaProximo = 7;
        }

        calendar.add(Calendar.DAY_OF_MONTH, diasHastaProximo);

        // Parsear la hora seleccionada y convertirla al formato adecuado (HH:mm)
        String horaFormateada = formatHora(horaSeleccionada);

        // Combinar fecha y hora
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(horaFormateada.split(":")[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(horaFormateada.split(":")[1]));
        calendar.set(Calendar.SECOND, 0); // Segundos predeterminados

        return sdf.format(calendar.getTime());
    }

    // Función para formatear la hora de 12h (am/pm) a 24h
    private String formatHora(String hora) {
        int horaInt;
        String minuto = "00";  // Asumimos 00 minutos si no se especifica

        if (hora.endsWith("am")) {
            horaInt = Integer.parseInt(hora.replace("am", "").trim());
            if (horaInt == 12) {
                horaInt = 0; // Si es "12am", debe ser 00:00
            }
        } else {
            horaInt = Integer.parseInt(hora.replace("pm", "").trim());
            if (horaInt != 12) {
                horaInt += 12; // Convertir a formato 24 horas
            }
        }

        return String.format("%02d:%s", horaInt, minuto);
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


