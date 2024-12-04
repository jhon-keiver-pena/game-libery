package com.example.library;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.example.gamelibery.R;
import com.example.library.service.HomeMaestro;
import com.example.library.service.UserService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Login extends AppCompatActivity {
    private EditText usuario, contraseña;
    private Button buttonini, buttonregis;
    private RadioButton rdUsuario, rdMaestro;
    private RadioGroup rdGroup;
    private ExecutorService executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Si este método no es estándar, asegúrate de que esté funcionando correctamente.
        setContentView(R.layout.activity_login);

        // Inicializar vistas después de setContentView
        usuario = findViewById(R.id.input_mail);
        contraseña = findViewById(R.id.input_password);
        buttonini = findViewById(R.id.btn_inicio);
        buttonregis = findViewById(R.id.btn_crear_maestro);
        rdUsuario = findViewById(R.id.rd_usuario);
        rdMaestro = findViewById(R.id.rd_maestro);
        rdGroup = findViewById(R.id.radioGroup); // Inicializar correctamente el RadioGroup aquí.

        // Inicializar el ExecutorService
        executor = Executors.newSingleThreadExecutor();

        // Configurar acciones para el botón de inicio
        buttonini.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String correo = usuario.getText().toString().trim();
                String clave = contraseña.getText().toString().trim();

                // Validar datos
                if (correo.isEmpty() || clave.isEmpty()) {
                    Toast.makeText(Login.this, "Los datos no están completos...", Toast.LENGTH_SHORT).show();
                    usuario.requestFocus();
                    return;
                }

                // Validar la selección del RadioButton
                if (rdUsuario.isChecked()) {
                    validateData(correo, clave);
                } else if (rdMaestro.isChecked()) {
                    validateDataMaestro(correo, clave);
                } else {
                    Toast.makeText(Login.this, "Seleccione si es maestro o usuario...", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Configurar acciones para el botón de registro
        buttonregis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int selectedId = rdGroup.getCheckedRadioButtonId();

                if (selectedId == R.id.rd_usuario) {
                    Intent intent = new Intent(Login.this, Registro.class);
                    startActivity(intent);
                } else if (selectedId == R.id.rd_maestro) {
                    Intent intent = new Intent(Login.this, RegistroMaestro.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(Login.this, "Seleccione la cuenta que desea registrar...", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void validateDataMaestro(String correo, String clave) {
        executor.execute(() -> {
            String response = "";
            try {
                // URL de la API para obtener todos los maestros
                URL url = new URL("https://ms-maestros-1078682117753.us-central1.run.app/v1/maestros");
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
                    boolean isValid = false;

                    // Buscar el maestro con las credenciales proporcionadas
                    for (int i = 0; i < maestrosArray.length(); i++) {
                        JSONObject maestro = maestrosArray.getJSONObject(i);
                        String maestroCorreo = maestro.getString("correo");
                        String maestroClave = maestro.getString("clave");

                        if (maestroCorreo.equals(correo) && maestroClave.equals(clave)) {
                            isValid = true;

                            // Guardar información del maestro en UserService
                            UserService userService = (UserService) getApplicationContext();
                            userService.getUsuario().setIdUsuario(maestro.getInt("id_maestro"));
                            userService.getUsuario().setNombre(maestro.getString("nombre"));
                            userService.getUsuario().setCorreo(maestroCorreo);
                            userService.getUsuario().setClave(maestroClave);
                            userService.getUsuario().setLogin(true);

                            // Guardar en SharedPreferences
                            SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putInt("idMaestro", maestro.getInt("id_maestro"));
                            editor.apply();

                            // Redirigir al Home
                            Intent intent = new Intent(getBaseContext(), HomeMaestro.class);
                            startActivity(intent);
                            finish();
                            break;
                        }
                    }

                    if (!isValid) {
                        response = "Credenciales inválidas";
                    }
                } else {
                    response = "Error al conectar con el servidor. Código: " + responseCode;
                }

                httpURLConnection.disconnect();
            } catch (Exception e) {
                response = "Error: " + e.getMessage();
            }

            // Mostrar el mensaje en un Toast (debe ejecutarse en el hilo principal)
            String finalResponseMessage = response;
            runOnUiThread(() -> Toast.makeText(Login.this, finalResponseMessage, Toast.LENGTH_LONG).show());
        });
    }



    private void validateData(String correo, String clave) {
        executor.execute(() -> {
            String response = "";
            try {
                // Integracion api python en Nube
                URL url = new URL("https://ms-usuarios-1078682117753.us-central1.run.app/v1/get-usuario-by-credentials?correo="
                        + correo + "&clave=" + clave);
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

                    // Convertir la respuesta a un objeto JSON
                    JSONObject jsonResponse = new JSONObject(responseBuilder.toString());

                    // Verificar que el objeto no sea null y tenga datos
                    if (jsonResponse != null && jsonResponse.length() > 0) {
                        UserService userService = (UserService) getApplicationContext();
                        userService.getUsuario().setIdUsuario(jsonResponse.getInt("id_usuario"));
                        userService.getUsuario().setNombre(jsonResponse.getString("nombre"));
                        userService.getUsuario().setApellido(jsonResponse.getString("apellido"));
                        userService.getUsuario().setTelefono(jsonResponse.getString("telefono"));
                        userService.getUsuario().setCorreo(jsonResponse.getString("correo"));
                        userService.getUsuario().setClave(jsonResponse.getString("clave"));
                        userService.getUsuario().setLogin(true);

                        // Guardar el idUsuario en SharedPreferences
                        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt("idUsuario", jsonResponse.getInt("id_usuario"));
                        editor.apply();

                        // Redirige a un activity si el usuario es válido
                        Intent intent = new Intent(getBaseContext(), Home.class);
                        startActivity(intent);
                        finish();
                    } else {
                        response = "Credenciales inválidas";
                    }
                } else {
                    response = "Credenciales inválidas";
                }

                httpURLConnection.disconnect();
            } catch (Exception e) {
                response = "Error: " + e.getMessage();
            }

            // Mostrar el resultado en un Toast (debe hacerse en el hilo principal)
            String finalResponse = response;
            runOnUiThread(() -> Toast.makeText(Login.this, finalResponse, Toast.LENGTH_LONG).show());
        });
    }



    private void validateData_OLD(String correo, String clave){
        executor.execute(() -> {
            String response;
            try {
                // URL de la API
                URL url = new URL("http://10.0.2.2:80/app-mobile/usuarios_api.php");
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                // Configurar la conexión
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setRequestProperty("Content-Type", "application/json");
                httpURLConnection.setDoOutput(true);

                // Crear el JSON con los datos
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("correo", correo);
                jsonParam.put("clave", clave);

                OutputStream os = httpURLConnection.getOutputStream();
                os.write(jsonParam.toString().getBytes(StandardCharsets.UTF_8));
                os.flush();
                os.close();

                int responseCode = httpURLConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    response = "Inicio de sesion";
                    //redirige a un activity
                    Intent intent = new Intent(getBaseContext(), Home.class);
                    startActivity(intent);
                    finish();
                } else {
                    response = "Credenciales incorrectas " + responseCode;
                }

                httpURLConnection.disconnect();
            } catch (Exception e) {
                response = "Error: " + e.getMessage();
            }

            // Mostrar el resultado en un Toast (debe hacerse en el hilo principal)
            String finalResponse = response;
            runOnUiThread(() -> Toast.makeText(Login.this, finalResponse, Toast.LENGTH_LONG).show());
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Limpiar el ExecutorService
        executor.shutdown();
    }
}