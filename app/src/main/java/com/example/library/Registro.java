package com.example.library;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gamelibery.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class Registro extends AppCompatActivity {

    //declaracion de variables
    private EditText nombreTxt;
    private EditText apellidoTxt;
    private EditText telefonoTxt;
    private EditText correoTxt;
    private EditText claveTxt;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro);

        //llamar por id
        nombreTxt = findViewById(R.id.editNombre);
        apellidoTxt = findViewById(R.id.editApellido);
        telefonoTxt = findViewById(R.id.editTelefono);
        correoTxt = findViewById(R.id.editCorreo);
        claveTxt = findViewById(R.id.editClave);
        button = findViewById(R.id.btn_crear);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Obtener los datos ingresados por el usuario
                String nombre = nombreTxt.getText().toString().trim();
                String apellido = apellidoTxt.getText().toString().trim();
                String telefono = telefonoTxt.getText().toString().trim();
                String correo = correoTxt.getText().toString().trim();
                String clave = claveTxt.getText().toString().trim();

                // Llamar método para realizar el registro
                if (!nombre.isEmpty() && !correo.isEmpty() && !clave.isEmpty()) {
                    registrarUsuario( nombre, apellido, telefono, correo,clave );
                } else {
                    Toast.makeText(Registro.this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    private void registrarUsuario(final String nombre, final String apellido,
                                  final String telefono, final String correo, final String clave) {

        // Hilo para manejar la petición HTTP en segundo plano
        new Thread(new Runnable() {
            @Override
            public void run() {
                String result;
                try {
                    // URL de la API
                    URL url = new URL("http://localhost/app-mobile/usuarios_api.php");

                    // Crear la conexión HTTP
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json; utf-8");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);

                    // Crear el JSON con los datos
                    JSONObject jsonInput = new JSONObject();
                    jsonInput.put("nombre", nombre);
                    jsonInput.put("apellido", apellido);
                    jsonInput.put("telefono", telefono);
                    jsonInput.put("correo", correo);
                    jsonInput.put("clave", clave);

                    // Escribir el JSON en la solicitud
                    OutputStream os = conn.getOutputStream();
                    byte[] input = jsonInput.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                    os.flush();
                    os.close();

                    // Leer la respuesta del servidor
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }

                    // Parsear la respuesta JSON
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    result = jsonResponse.getString("mensaje");

                    // Mostrar respuesta en el hilo principal (UI Thread)
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Registro.this,
                                    "Respuesta del servidor: " + result, Toast.LENGTH_LONG).show();
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    // Mostrar error en el hilo principal (UI Thread)
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Registro.this, "Error en la conexión: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }


}