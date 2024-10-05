package com.example.library;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gamelibery.R;
import com.example.library.model.MaestroAdapter;
import com.example.library.model.rest.Maestro;

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

public class BibliotecaMaestro extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MaestroAdapter adapter;
    private List<Maestro> maestroList;
    private ExecutorService executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_biblioteca_maestro);

        recyclerView = findViewById(R.id.recyclerViewMaestros);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        maestroList = new ArrayList<>();
        adapter = new MaestroAdapter(maestroList, this);
        recyclerView.setAdapter(adapter);

        // Inicializar el ExecutorService
        executor = Executors.newSingleThreadExecutor();

        // Cargar los maestros
        loadMaestros();
    }

    private void loadMaestros() {
        executor.execute(() -> {
            List<Maestro> maestros = new ArrayList<>();
            try {
                URL url = new URL("http://10.0.2.2:80/app-mobile/maestro_api.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                // Configura el timeout (en milisegundos)
                conn.setConnectTimeout(8000); // Tiempo de espera de conexión
                conn.setReadTimeout(8000);    // Tiempo de espera de lectura

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONArray jsonArray = new JSONArray(response.toString());
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Maestro maestro = new Maestro(
                            jsonObject.getInt("id_maestro"),
                            jsonObject.getString("nombre"),
                            jsonObject.getString("telefono"),
                            jsonObject.getString("edad"),
                            jsonObject.getString("sexo"),
                            jsonObject.getString("experiencia"),
                            jsonObject.getString("tiempo_campo"),
                            jsonObject.getString("especialidad"),
                            jsonObject.getString("url_imagen")
                    );
                    maestros.add(maestro);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Actualizar la UI en el hilo principal
            runOnUiThread(() -> {
                maestroList.clear();
                maestroList.addAll(maestros);
                adapter.notifyDataSetChanged();
                if (maestros.isEmpty()) {
                    Toast.makeText(this, "No se encontraron maestros", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getBaseContext(), Home.class);
                    startActivity(intent);
                    finish();
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown(); // Limpiar el ExecutorService
    }
}
