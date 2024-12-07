package com.example.library;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DetalleReserva extends AppCompatActivity {

    ExecutorService executor;

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
        Spinner comuna = findViewById(R.id.spinner_info_ciudad);
        Spinner spinnerEstado = findViewById(R.id.spinner_info_estado);
        Spinner dias = findViewById(R.id.spinnerDiasDetalle);
        Spinner horas = findViewById(R.id.spinnerHoraDetalle);


        // Configurar los valores de los spinners
        configurarSpinners(comuna, spinnerEstado, dias, horas);

        // Obtener los detalles de la reserva
        obtenerDetalleReserva(idReserva, infoMaestro, comuna, spinnerEstado, dias);

        // Configurar los botones
        Button btnActualizar = findViewById(R.id.btn_actualizar);
        Button btnVolver = findViewById(R.id.btn_volver);
        Button btnHome = findViewById(R.id.btn_home);


        btnActualizar.setOnClickListener(v -> {
            String diaSeleccionado = dias.getSelectedItem() != null ? dias.getSelectedItem().toString() : "";
            String horaSeleccionada = horas.getSelectedItem() != null ? horas.getSelectedItem().toString() : "";
            String fechaVisita = obtenerFechaConHora(diaSeleccionado, horaSeleccionada);
            actualizarReserva(idReserva,
                    fechaVisita,
                    comuna.getSelectedItem().toString(),
                    spinnerEstado.getSelectedItemPosition() + 1);
                } );

        btnVolver.setOnClickListener(v -> finish());
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(DetalleReserva.this, Home.class);
            startActivity(intent);
            finish();
        });
    }

    // Método para configurar los spinners de ciudad y estado
    private void configurarSpinners(Spinner comuna, Spinner spinnerEstado, Spinner dias, Spinner horas) {
        // Crear adaptadores para los spinners
        ArrayAdapter<CharSequence> adapterComuna = ArrayAdapter.createFromResource(
                this, R.array.comunas, android.R.layout.simple_spinner_item);
        comuna.setAdapter(adapterComuna);

        ArrayAdapter<CharSequence> adapterEstado = ArrayAdapter.createFromResource(
                this, R.array.estados, android.R.layout.simple_spinner_item);
        spinnerEstado.setAdapter(adapterEstado);

        ArrayAdapter<CharSequence> adapterDias = ArrayAdapter.createFromResource(
                this, R.array.dias, android.R.layout.simple_spinner_item);
        dias.setAdapter(adapterDias);

        // Adaptador para el spinner de horas
        ArrayAdapter<CharSequence> adapterHoras = ArrayAdapter.createFromResource(
                this, R.array.horas, android.R.layout.simple_spinner_item);
        horas.setAdapter(adapterHoras);

    }

    private void obtenerDetalleReserva(int idReserva, TextView infoMaestro, Spinner spinnerCiudad,
                                       Spinner spinnerEstado, Spinner spinnerDia) {
        if (executor == null || executor.isShutdown()) {
            executor = Executors.newSingleThreadExecutor();
        }

        executor.execute(() -> {
            try {
                Log.d("DetalleReserva", "obtenerDetalleReserva ejecutado con idReserva: " + idReserva);

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
                    String fechaVisita = jsonReserva.getString("fecha_visita");

                    String diaSemana = obtenerDiaSemana(fechaVisita);

                    // Extraer el nombre del maestro
                    String nombreMaestro = getMaestro(maestro);

                    // Actualizar la interfaz de usuario en el hilo principal
                    runOnUiThread(() -> {
                        infoMaestro.setText(nombreMaestro);

                        ArrayAdapter<CharSequence> ciudadAdapter = (ArrayAdapter<CharSequence>) spinnerCiudad.getAdapter();
                        int ciudadPosition = ciudadAdapter.getPosition(ciudad);
                        spinnerCiudad.setSelection(ciudadPosition);

                        spinnerEstado.setSelection(estado == 1 ? 0 : 1);

                        // Actualizar el spinner del día
                        ArrayAdapter<CharSequence> diaAdapter = (ArrayAdapter<CharSequence>) spinnerDia.getAdapter();
                        int diaPosition = diaAdapter.getPosition(diaSemana);
                        spinnerDia.setSelection(diaPosition);
                    });

                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Error al obtener la reserva: " + responseCode, Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }



    private String getMaestro(String idMaestro) {
        try {
            URL url = new URL("https://ms-maestros-1078682117753.us-central1.run.app/v1/maestros/" + idMaestro);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setRequestProperty("Content-Type", "application/json");

            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                StringBuilder responseBuilder = new StringBuilder();
                try (BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()))) {
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        responseBuilder.append(inputLine);
                    }
                }

                // Convertir el resultado en un JSONObject
                JSONObject maestro = new JSONObject(responseBuilder.toString());
                return maestro.getString("nombre"); // Devolver el campo "nombre"
            }
        } catch (Exception e) {
            runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
        return "";
    }

    private String obtenerDiaSemana(String fechaVisita) {
        try {
            // Formateador para la fecha
            DateTimeFormatter formatter = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                        .withZone(ZoneId.systemDefault());
            }
            LocalDateTime dateTime = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                dateTime = LocalDateTime.parse(fechaVisita, formatter);
            }

            // Convertir a LocalDate
            LocalDate date = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                date = dateTime.toLocalDate();
            }

            // Obtener el día de la semana como texto (en español)
            DayOfWeek dia = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                dia = date.getDayOfWeek();
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                switch (dia) {
                    case MONDAY:
                        return "Lunes";
                    case TUESDAY:
                        return "Martes";
                    case WEDNESDAY:
                        return "Miércoles";
                    case THURSDAY:
                        return "Jueves";
                    case FRIDAY:
                        return "Viernes";
                    case SATURDAY:
                        return "Sábado";
                    case SUNDAY:
                        return "Domingo";
                    default:
                        return "";
                }
            }
        } catch (Exception e) {
            Log.e("ObtenerDiaSemana", "Error al procesar la fecha: " + e.getMessage());
            return "";
        }
        return "";
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

    private void destroyExecutor() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }


}


