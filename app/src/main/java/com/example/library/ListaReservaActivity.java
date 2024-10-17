package com.example.library;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Validar si esta logeado
        UserService userService = (UserService) getApplicationContext();
        if (!userService.getUsuario().isLogin()){
            Toast.makeText(this, "Debes iniciar Sesion para acceder a esta pantalla",
                    Toast.LENGTH_SHORT).show();
            //redirige a un activity
            Intent intent = new Intent(getBaseContext(), Login.class);
            startActivity(intent);
            finish();
        }
        EdgeToEdge.enable(this);
        setContentView(R.layout.lista_reserva_activity);


        // Inicializar el ExecutorService
        listViewReservas = findViewById(R.id.listViewReservas);
        executor = Executors.newSingleThreadExecutor();

        // Inicializar listas
        maestroInfoList = new ArrayList<>();
        reservaElements = new ArrayList<>();

        // Crear el adaptador para mostrar la lista en el ListView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, maestroInfoList);
        listViewReservas.setAdapter(adapter);
        obtenerReservasPorUsuario(userService.getUsuario().getIdUsuario());
    }

    private void obtenerReservasPorUsuario(int idUsuario) {
        executor.execute(() -> {
            String response = "";
            List<ReservaElement> reservasList = new ArrayList<>();
            List<String> reservasInfo = new ArrayList<>();
            try {
                // URL de la API con el ID del usuario
                URL url = new URL("http://10.0.2.2:80/app-mobile/reserva_api.php/obtain-by-id/" + idUsuario);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                // Configurar la conexión
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setRequestProperty("Content-Type", "application/json");
                httpURLConnection.setDoInput(true); // Para leer la respuesta

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

                    // Convertir la respuesta a un array JSON
                    JSONArray jsonArray = new JSONArray(responseBuilder.toString());

                    // Lista para almacenar las reservas

                    // Recorrer el array JSON y convertirlo en objetos Reserva
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonReserva = jsonArray.getJSONObject(i);
                        ReservaElement reserva = new ReservaElement();
                        reserva.setIdReserva(jsonReserva.getInt("id_reserva"));
                        reserva.setIdMaestro(jsonReserva.getInt("id_maestro"));
                        reserva.setFechaVisita(jsonReserva.getString("fecha_visita"));
                        reserva.setCoste(jsonReserva.getDouble("coste"));
                        reserva.setCiudad(jsonReserva.getString("ciudad"));
                        reserva.setIdUsuario(jsonReserva.getInt("id_usuario"));

                        reservasList.add(reserva); // Agregar reserva a la lista
                        reservasInfo.add(reserva.getIdReserva() +
                                " - " + reserva.getFechaVisita()+ " - " +reserva.getCiudad()
                        + " - " +reserva.getIdMaestro());
                    }


                } else {
                    response = "Error en la solicitud, código de respuesta: " + responseCode;
                }

                httpURLConnection.disconnect();
            } catch (Exception e) {
                response = "Error: " + e.getMessage();
            }
            // Actualizar la UI en el hilo principal
            runOnUiThread(() -> {
                reservaElements.clear();
                reservaElements.addAll(reservasList);
                maestroInfoList.clear();
                maestroInfoList.addAll(reservasInfo);


            });
     });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown(); // Limpiar el ExecutorService
    }

}