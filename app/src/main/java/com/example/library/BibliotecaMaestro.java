package com.example.library;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gamelibery.R;
import com.example.library.model.rest.Maestro;
import com.example.library.service.UserService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class BibliotecaMaestro extends AppCompatActivity {

    private ListView listViewMaestros;
    private ArrayAdapter<String> adapter;
    private List<Maestro> maestroList;
    private List<String> maestroInfoList;

    private Spinner spinnerCategorias;
    private List<Categoria> listaCategorias;
    private ExecutorService executor;

    private Button btnVolverHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Validar si está logueado
        UserService userService = (UserService) getApplicationContext();
        if (!userService.getUsuario().isLogin()) {
            Toast.makeText(this, "Debes iniciar sesión para acceder a esta pantalla",
                    Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getBaseContext(), Login.class);
            startActivity(intent);
            finish();
        }
        setContentView(R.layout.activity_biblioteca_maestro);

        // Inicializar elementos de la UI
        btnVolverHome = findViewById(R.id.btn_home);
        listViewMaestros = findViewById(R.id.listViewReservas);
        spinnerCategorias = findViewById(R.id.spinner2);

        maestroInfoList = new ArrayList<>();
        maestroList = new ArrayList<>();
        listaCategorias = new ArrayList<>();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, maestroInfoList);
        listViewMaestros.setAdapter(adapter);

        executor = Executors.newSingleThreadExecutor();

        // Cargar las categorías
        loadCategorias();

        // Configurar el Spinner
        spinnerCategorias.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    // Si se selecciona la opción "Seleccione una categoría", no hacer nada
                    return;
                }
                int idCategoriaSeleccionada = listaCategorias.get(position - 1).getIdCategoria(); // -1 para evitar la primera opción
                loadMaestrosPorCategoria(idCategoriaSeleccionada);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hacer nada si no se selecciona ninguna opción
            }
        });

        // Configurar la acción del botón "Volver a Home"
        btnVolverHome.setOnClickListener(view -> {
            Intent intent = new Intent(getBaseContext(), Home.class);
            startActivity(intent);
            finish();
        });

        // Configurar clic en los elementos del ListView
        listViewMaestros.setOnItemClickListener((parent, view, position, id) -> {
            Maestro selectedMaestro = maestroList.get(position);
            Intent intent = new Intent(BibliotecaMaestro.this, DetalleMaestro.class);
            intent.putExtra("maestro", selectedMaestro);
            startActivity(intent);
        });
    }

    private void loadCategorias() {
        executor.execute(() -> {
            try {
                URL url = new URL("https://ms-categorias-1078682117753.us-central1.run.app/v1/categorias");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONArray jsonArray = new JSONArray(response.toString());
                List<String> nombresCategorias = new ArrayList<>();

                // Añadir la opción inicial al Spinner
                nombresCategorias.add("Seleccione una categoría"); // Primera opción del Spinner

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Categoria categoria = new Categoria(
                            jsonObject.getInt("id_categoria"),
                            jsonObject.getString("especialidad")
                    );
                    listaCategorias.add(categoria);
                    nombresCategorias.add(categoria.getEspecialidad());
                }

                runOnUiThread(() -> {
                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(BibliotecaMaestro.this,
                            android.R.layout.simple_spinner_item, nombresCategorias);
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerCategorias.setAdapter(spinnerAdapter);

                    // Establecer la selección inicial en "Seleccione una categoría"
                    spinnerCategorias.setSelection(0);
                });

            } catch (Exception e) {
                Log.d("Carga-categorias", "Error al cargar categorías: ", e);
            }
        });
    }

    private void loadMaestrosPorCategoria(int idCategoria) {
        executor.execute(() -> {
            List<Maestro> maestros = new ArrayList<>();
            List<String> maestrosInfo = new ArrayList<>();
            try {
                URL url = new URL("https://ms-maestros-1078682117753.us-central1.run.app/v1/maestros/categoria/" + idCategoria);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONArray jsonArray = new JSONArray(response.toString());
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Maestro maestro = new Maestro(
                            jsonObject.getInt("id_maestro"),
                            jsonObject.getInt("id_categoria"),
                            jsonObject.getString("nombre"),
                            jsonObject.getString("telefono"),
                            jsonObject.getString("edad"),
                            jsonObject.getString("sexo"),
                            jsonObject.getString("experiencia"),
                            formatter.parse(jsonObject.getString("tiempo_campo")),
                            jsonObject.getString("correo"),
                            jsonObject.getString("clave"),
                            null
                    );
                    maestro.setNombreCategoria(listaCategorias.stream()
                            .filter(c -> c.getIdCategoria() == maestro.getIdCategoria())
                            .findFirst()
                            .map(Categoria::getEspecialidad)
                            .orElse("Desconocida"));

                    maestros.add(maestro);
                    maestrosInfo.add(maestro.getNombre() + " - " + maestro.getNombreCategoria());
                }
            } catch (Exception e) {
                Log.d("Carga-maestros", "Error al cargar maestros: ", e);
            }

            runOnUiThread(() -> {
                maestroList.clear();
                maestroList.addAll(maestros);
                maestroInfoList.clear();
                maestroInfoList.addAll(maestrosInfo);
                adapter.notifyDataSetChanged();
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}


