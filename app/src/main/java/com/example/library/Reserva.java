package com.example.library;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.widget.Button;
import android.widget.TextView;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Locale;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gamelibery.R;
import com.example.library.model.rest.Maestro;
import com.example.library.service.UserService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Reserva extends AppCompatActivity {

    private TextView info;
    private Button confirmar, volverHome, volverCotizar;
    private ExecutorService executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Validar si el usuario está logeado
        UserService userService = (UserService) getApplicationContext();
        if (!userService.getUsuario().isLogin()) {
            Toast.makeText(this, "Debes iniciar sesión para acceder a esta pantalla",
                    Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getBaseContext(), Login.class);
            startActivity(intent);
            finish();
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reserva);

        info = findViewById(R.id.txt_Info);
        confirmar = findViewById(R.id.btn_confirmar);
        volverHome = findViewById(R.id.btnVHome);
        volverCotizar = findViewById(R.id.btn_VCotizar);

        Intent intent = getIntent();
        String comunaSeleccionada = intent.getStringExtra("comuna");
        String diaSeleccionado = intent.getStringExtra("dia");
        double valorCotizacion = intent.getDoubleExtra("valor_cotizacion", 0);

        info.setText("Comuna: " + comunaSeleccionada + "\nDía: " + diaSeleccionado + "\nValor: $" + valorCotizacion);

        executor = Executors.newSingleThreadExecutor();

        volverHome.setOnClickListener(view -> {
            Intent homeIntent = new Intent(getBaseContext(), Home.class);
            startActivity(homeIntent);
            finish();
        });

        volverCotizar.setOnClickListener(view -> {
            Intent cotizarIntent = new Intent(getBaseContext(), Cotizar.class);
            startActivity(cotizarIntent);
            finish();
        });

        confirmar.setOnClickListener(view -> realizarReserva());
    }

    private void realizarReserva() {
        Maestro maestro = (Maestro) getIntent().getSerializableExtra("maestro");
        int idMaestro = maestro != null ? maestro.getId() : -1;

        UserService userService = (UserService) getApplicationContext();
        int idUsuario = userService.getUsuario().getIdUsuario();

        String comunaSeleccionada = getIntent().getStringExtra("comuna");
        String diaSeleccionado = getIntent().getStringExtra("dia");
        double valorCotizacion = getIntent().getDoubleExtra("valor_cotizacion", 0.0);

        if (idMaestro == -1 || comunaSeleccionada.isEmpty() || diaSeleccionado.isEmpty() || idUsuario == -1) {
            Toast.makeText(Reserva.this, "Datos inválidos para realizar la reserva", Toast.LENGTH_SHORT).show();
            return;
        }

        String fechaVisita = obtenerFechaPorNombreDia(diaSeleccionado);
        if (fechaVisita == null) {
            Toast.makeText(this, "Error al obtener la fecha", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject reservaJSON = new JSONObject();
        try {
            reservaJSON.put("id_maestro", idMaestro);
            reservaJSON.put("fecha_visita", fechaVisita);
            reservaJSON.put("coste", valorCotizacion);
            reservaJSON.put("ciudad", comunaSeleccionada);
            reservaJSON.put("id_usuario", idUsuario);
            reservaJSON.put("id_estado", 1); // Estado inicial de la reserva
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(Reserva.this, "Error al generar la reserva", Toast.LENGTH_SHORT).show();
            return;
        }

        sendReservation(reservaJSON);
    }

    private String obtenerFechaPorNombreDia(String diaNombre) {
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

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        calendar.set(Calendar.HOUR_OF_DAY, 17); // Hora predeterminada
        calendar.set(Calendar.MINUTE, 30);     // Minutos predeterminados
        calendar.set(Calendar.SECOND, 0);      // Segundos predeterminados
        return sdf.format(calendar.getTime());
    }

    private void sendReservation(JSONObject reservaJSON) {
        executor.execute(() -> {
            try {
                URL url = new URL("https://ms-reserva-1078682117753.us-central1.run.app/v1/reservas");
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Content-Type", "application/json");
                httpURLConnection.setDoOutput(true);

                try (OutputStream os = httpURLConnection.getOutputStream()) {
                    os.write(reservaJSON.toString().getBytes(StandardCharsets.UTF_8));
                    os.flush();
                }

                int responseCode = httpURLConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_CREATED) {
                    runOnUiThread(() -> {
                        Toast.makeText(Reserva.this, "Reserva realizada con éxito", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getBaseContext(), Home.class);
                        startActivity(intent);
                        finish();
                    });
                } else {
                    InputStream errorStream = httpURLConnection.getErrorStream();
                    String errorResponse = "";
                    if (errorStream != null) {
                        errorResponse = new BufferedReader(new InputStreamReader(errorStream))
                                .lines()
                                .reduce("", (acc, line) -> acc + line);
                    } else {
                        errorResponse = "No se pudo procesar el error del servidor.";
                    }
                    String finalErrorResponse = errorResponse;
                    runOnUiThread(() -> Toast.makeText(Reserva.this, "Error: " + finalErrorResponse, Toast.LENGTH_LONG).show());
                }

                httpURLConnection.disconnect();
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(Reserva.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}

