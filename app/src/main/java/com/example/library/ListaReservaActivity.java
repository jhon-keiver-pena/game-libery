package com.example.library;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gamelibery.R;
import com.example.library.model.rest.ReservaElement;
import com.example.library.service.UserService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ListaReservaActivity extends AppCompatActivity {

    private ExecutorService executor;
    private List<String> maestroInfoList;
    private ListView listViewReservas;
    private List<ReservaElement> reservaElements;
    private Button btnHome;
    private ArrayAdapter<String> adapter; // Adaptador global
    private CheckBox checkActivas, checkCanceladas; // CheckBox para filtrar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Validar si está logueado
        UserService userService = (UserService) getApplicationContext();
        if (!userService.getUsuario().isLogin()) {
            Toast.makeText(this, "Debes iniciar sesión para acceder a esta pantalla", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getBaseContext(), Login.class);
            startActivity(intent);
            finish();
            return; // Evitar seguir ejecutando el código si el usuario no está logueado
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.lista_reserva_activity);

        btnHome = findViewById(R.id.btn_home);
        btnHome.setOnClickListener(view -> {
            Intent intent = new Intent(getBaseContext(), Home.class);
            startActivity(intent);
            finish();
        });

        listViewReservas = findViewById(R.id.listViewReservas);
        executor = Executors.newSingleThreadExecutor();

        // Inicializar listas
        maestroInfoList = new ArrayList<>();
        reservaElements = new ArrayList<>();

        // Crear el adaptador para mostrar la lista en el ListView
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, maestroInfoList);
        listViewReservas.setAdapter(adapter);

        // Configurar el listener para los clics en los elementos del ListView
        listViewReservas.setOnItemClickListener((parent, view, position, id) -> {
            // Obtener el objeto `ReservaElement` correspondiente a la posición seleccionada
            ReservaElement reservaSeleccionada = reservaElements.get(position);

            // Crear un intent para abrir DetalleReserva y pasar los datos de la reserva
            Intent intent = new Intent(ListaReservaActivity.this, DetalleReserva.class);
            intent.putExtra("idReserva", reservaSeleccionada.getIdReserva());
            intent.putExtra("fechaVisita", reservaSeleccionada.getFechaVisita());
            intent.putExtra("ciudad", reservaSeleccionada.getCiudad());
            intent.putExtra("coste", reservaSeleccionada.getCoste());
            intent.putExtra("estado", reservaSeleccionada.getEstado());
            intent.putExtra("idMaestro", reservaSeleccionada.getIdMaestro());

            // Iniciar la actividad DetalleReserva
            startActivity(intent);
        });

        // Inicializar los CheckBox
        checkActivas = findViewById(R.id.check_activas);
        checkCanceladas = findViewById(R.id.check_canceladas);

        // Configurar los listeners para los CheckBox
        checkActivas.setOnCheckedChangeListener((buttonView, isChecked) -> actualizarLista());
        checkCanceladas.setOnCheckedChangeListener((buttonView, isChecked) -> actualizarLista());

        // Obtener reservas para el usuario actual, pero no mostrar nada al inicio
        obtenerReservasPorUsuario(userService.getUsuario().getIdUsuario());
    }

    private void obtenerReservasPorUsuario(int idUsuario) {
        executor.execute(() -> {
            List<ReservaElement> reservasList = new ArrayList<>();
            List<String> reservasInfo = new ArrayList<>();

            try {
                // Construir la URL con el ID del usuario
                URL url = new URL("https://ms-reserva-1078682117753.us-central1.run.app/v1/reservas/usuario/" + idUsuario);
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

                    // Parsear la respuesta JSON
                    JSONArray jsonArray = new JSONArray(responseBuilder.toString());
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonReserva = jsonArray.getJSONObject(i);

                        ReservaElement reserva = new ReservaElement();
                        reserva.setIdReserva(jsonReserva.getInt("id_reserva"));
                        reserva.setIdMaestro(jsonReserva.getInt("id_maestro"));
                        reserva.setFechaVisita(jsonReserva.getString("fecha_visita"));
                        reserva.setCoste(jsonReserva.getDouble("coste"));
                        reserva.setCiudad(jsonReserva.getString("ciudad"));
                        reserva.setEstado(jsonReserva.getInt("id_estado"));

                        reservasList.add(reserva);
                        reservasInfo.add(reserva.getIdReserva() +
                                " - " + reserva.getFechaVisita() + " - " + reserva.getCiudad() +
                                " - " + reserva.getEstado());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "No hay Reservas disponibles: " + responseCode, Toast.LENGTH_SHORT).show());
                }

                connection.disconnect();
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            // Guardar la información de las reservas, pero no actualizar la UI aún
            runOnUiThread(() -> {
                reservaElements.clear();
                reservaElements.addAll(reservasList);
                maestroInfoList.clear();
                adapter.notifyDataSetChanged();
            });
        });
    }

    // Método para filtrar las reservas basadas en los CheckBox seleccionados
    private void actualizarLista() {
        List<String> reservasFiltradas = new ArrayList<>();
        for (ReservaElement reserva : reservaElements) {
            boolean mostrar = false;

            // Si se selecciona el CheckBox de "ACTIVAS" y el estado de la reserva es activo
            if (checkActivas.isChecked() && reserva.getEstado() == 1) { // 1 = Activa
                checkCanceladas.setChecked(false);
                mostrar = true;

            }

            // Si se selecciona el CheckBox de "CANCELADAS" y el estado de la reserva es cancelado
            if (checkCanceladas.isChecked() && reserva.getEstado() == 2) { // 2 = Cancelada
                checkActivas.setChecked(false);
                mostrar = true;

            }

            // Si se debe mostrar la reserva, la añadimos a la lista filtrada
            if (mostrar) {
                reservasFiltradas.add(reserva.getIdReserva() + " - " + reserva.getFechaVisita() + " - " + reserva.getCiudad());
            }
        }

        // Actualizamos la lista en la UI solo después de que se seleccione algún checkbox
        maestroInfoList.clear();
        maestroInfoList.addAll(reservasFiltradas);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown(); // Limpiar el ExecutorService
    }
}



