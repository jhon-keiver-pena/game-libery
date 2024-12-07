package com.example.library;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gamelibery.R;
import com.example.library.model.UsuarioContexto;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class EditarUsuario extends AppCompatActivity {

    private EditText editNombre, editApellido, editPhone, editEmail, editPassword;
    private Button bttGuardar, bttEliminar;
    private Integer idUsuario;  // Esto se recuperará de SharedPreferences

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_usuario);

        // Inicializar vistas
        editNombre = findViewById(R.id.editNombre);
        editApellido = findViewById(R.id.editApellido);
        editPhone = findViewById(R.id.editPhone);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        bttGuardar = findViewById(R.id.button2);
        bttEliminar = findViewById(R.id.button);

        // Recuperar el idUsuario desde SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        idUsuario = sharedPreferences.getInt("idUsuario", -1); // Recuperar idUsuario

        if (idUsuario == -1) {
            Toast.makeText(this, "Usuario no encontrado", Toast.LENGTH_SHORT).show();
            finish(); // Si no hay idUsuario, termina la actividad
            return;
        }

        // Obtener la información existente del usuario
        obtenerUsuario();

        // Configurar botones
        bttGuardar.setOnClickListener(v -> modificarUsuario());
        bttEliminar.setOnClickListener(v -> eliminarCuenta());
    }

    private void obtenerUsuario() {
        new GetUserTask(idUsuario).execute();
    }

    private void modificarUsuario() {
        UsuarioContexto usuario = new UsuarioContexto();
        usuario.setIdUsuario(idUsuario);
        usuario.setNombre(editNombre.getText().toString());
        usuario.setApellido(editApellido.getText().toString());
        usuario.setTelefono(editPhone.getText().toString());
        usuario.setCorreo(editEmail.getText().toString());
        usuario.setClave(editPassword.getText().toString());

        new UpdateUserTask(usuario).execute();
    }

    // Tarea para obtener la información existente del usuario
    private class GetUserTask extends AsyncTask<Void, Void, UsuarioContexto> {
        private Integer idUsuario;

        public GetUserTask(Integer idUsuario) {
            this.idUsuario = idUsuario;
        }

        @Override
        protected UsuarioContexto doInBackground(Void... voids) {
            try {
                URL url = new URL("https://ms-usuarios-1078682117753.us-central1.run.app/v1/get-usuario/" + idUsuario);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                // Configurar la conexión
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setRequestProperty("Content-Type", "application/json");

                // Leer la respuesta
                int responseCode = httpURLConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    StringBuilder responseBuilder = new StringBuilder();
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()))) {
                        String inputLine;
                        while ((inputLine = in.readLine()) != null) {
                            responseBuilder.append(inputLine);
                        }
                    }

                    JSONObject jsonResponse = new JSONObject(responseBuilder.toString());
                    UsuarioContexto usuario = new UsuarioContexto();
                    usuario.setIdUsuario(jsonResponse.getInt("id_usuario"));
                    usuario.setNombre(jsonResponse.getString("nombre"));
                    usuario.setApellido(jsonResponse.getString("apellido"));
                    usuario.setTelefono(jsonResponse.getString("telefono"));
                    usuario.setCorreo(jsonResponse.getString("correo"));
                    usuario.setClave(jsonResponse.getString("clave"));

                    return usuario;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(UsuarioContexto usuario) {
            if (usuario != null) {
                // Poblar los campos con la información existente
                editNombre.setText(usuario.getNombre());
                editApellido.setText(usuario.getApellido());
                editPhone.setText(usuario.getTelefono());
                editEmail.setText(usuario.getCorreo());
                editPassword.setText(usuario.getClave());
            } else {
                Toast.makeText(EditarUsuario.this, "Error al cargar la información del usuario", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void eliminarCuenta() {
        // Ejecutar la tarea para eliminar la cuenta
        new DeleteUserTask(idUsuario).execute();
    }

    // Tarea para actualizar el usuario
    private class UpdateUserTask extends AsyncTask<Void, Void, String> {
        private UsuarioContexto usuario;

        public UpdateUserTask(UsuarioContexto usuario) {
            this.usuario = usuario;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                // URL para la actualización
                URL url = new URL("https://ms-usuarios-1078682117753.us-central1.run.app/v1/update-usuario/" + usuario.getIdUsuario());
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                // Configurar la conexión
                httpURLConnection.setRequestMethod("PUT");
                httpURLConnection.setRequestProperty("Content-Type", "application/json");
                httpURLConnection.setDoOutput(true);

                // Crear el objeto JSON con los datos del usuario
                JSONObject json = new JSONObject();
                json.put("nombre", usuario.getNombre());
                json.put("apellido", usuario.getApellido());
                json.put("telefono", usuario.getTelefono());
                json.put("correo", usuario.getCorreo());
                json.put("clave", usuario.getClave());

                // Enviar el JSON al servidor
                try (DataOutputStream os = new DataOutputStream(httpURLConnection.getOutputStream())) {
                    os.writeBytes(json.toString());
                    os.flush();
                }

                // Obtener la respuesta del servidor
                int responseCode = httpURLConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Intent intent = new Intent(getBaseContext(), Home.class);
                    startActivity(intent);
                    finish();
                    return "Información actualizada";

                } else {
                    return "Error al actualizar: " + responseCode;
                }

            } catch (Exception e) {
                return "Fallo en la conexión: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(EditarUsuario.this, result, Toast.LENGTH_SHORT).show();
        }
    }

    // Tarea para eliminar la cuenta
    private class DeleteUserTask extends AsyncTask<Void, Void, String> {
        private Integer idUsuario;

        public DeleteUserTask(Integer idUsuario) {
            this.idUsuario = idUsuario;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                // URL para eliminar el usuario
                URL url = new URL("https://ms-usuarios-1078682117753.us-central1.run.app/v1/delete-usuario/" + idUsuario);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                // Configurar la conexión
                httpURLConnection.setRequestMethod("DELETE");
                httpURLConnection.setRequestProperty("Content-Type", "application/json");

                // Obtener la respuesta del servidor
                int responseCode = httpURLConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                    Intent intent = new Intent(getBaseContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                    return "Cuenta eliminada";
                } else {
                    return "Error al eliminar cuenta: " + responseCode;
                }

            } catch (Exception e) {
                return "Fallo en la conexión: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(EditarUsuario.this, result, Toast.LENGTH_SHORT).show();
            if (result.equals("Cuenta eliminada")) {
                finish(); // Cierra la actividad
            }
        }
    }
}
