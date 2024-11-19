package com.example.library;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
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
    private List<Maestro> maestroList; // Lista completa de objetos Maestro
    private List<String> maestroInfoList; // Lista para mostrar solo nombre y especialidad
    private ExecutorService executor;

    private Button btnVolverHome;

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
        setContentView(R.layout.activity_biblioteca_maestro);

        btnVolverHome = findViewById(R.id.btn_home);
        // Inicializar ListView y listas
        listViewMaestros = findViewById(R.id.listViewReservas);
        maestroInfoList = new ArrayList<>();
        maestroList = new ArrayList<>(); // Aquí almacenamos los objetos completos

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, maestroInfoList);
        listViewMaestros.setAdapter(adapter);

        // Inicializar el ExecutorService
        executor = Executors.newSingleThreadExecutor();

        // Cargar los maestros
        loadMaestros();

        // Configurar la acción al hacer clic en un elemento de la lista
        listViewMaestros.setOnItemClickListener((parent, view, position, id) -> {
            Maestro selectedMaestro = maestroList.get(position); // Obtenemos el maestro seleccionado

            // Pasamos el objeto Maestro al siguiente Activity
            Intent intent = new Intent(BibliotecaMaestro.this, DetalleMaestro.class);
            intent.putExtra("maestro", selectedMaestro); // Pasamos el objeto Maestro (debe ser Serializable o Parcelable)
            startActivity(intent);
        });

        btnVolverHome.setOnClickListener(view -> {
            //redirige a un activity
            Intent intent = new Intent(getBaseContext(), Home.class);
            startActivity(intent);
            finish();
        });
    }

    private void loadMaestros() {
        executor.execute(() -> {
            List<Maestro> maestros = new ArrayList<>();
            List<String> maestrosInfo = new ArrayList<>();
            try {
                URL url = new URL("https://ms-maestros-1078682117753.us-central1.run.app/v1/maestros");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

                // Parsear el JSON y extraer los detalles completos de cada maestro
                JSONArray jsonArray = new JSONArray(response.toString());
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    String imageBase64 = jsonObject.isNull("image") ? null : jsonObject.optString("image"); // Usar optString para evitar errores
                    byte[] imageBytes = null;
                    if (imageBase64 !=null){
                        try {
                            imageBytes = Base64.decode(imageBase64, Base64.DEFAULT);
                        } catch (IllegalArgumentException e) {
                            Log.d("Decodificacion-imagen", "Error al decodificar immagen : {}", e);
                        }
                    }

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
                            imageBytes
                    );
                    //TODO: Traer las categorias en una lista y el nombre
                    // irlo asignando a cada maestro segun el id_categoria
                    maestro.setNombreCategoria("Categoria Test");
                    maestros.add(maestro);
                    maestrosInfo.add(maestro.getNombre() + " - " + maestro.getNombreCategoria()); // Solo nombre y especialidad para mostrar en el ListView
                }
            } catch (Exception e) {
                Log.d("Lidado-maestros", "Error en listar : {}", e);
            }

            // Actualizar la UI en el hilo principal
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
        executor.shutdown(); // Limpiar el ExecutorService
    }
}
