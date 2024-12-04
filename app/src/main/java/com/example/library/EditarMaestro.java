package com.example.library;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gamelibery.R;
import com.example.library.model.rest.Maestro;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class EditarMaestro extends AppCompatActivity {

    private EditText editNombre, editPhone, editSexo, editEdad, editExperiencia, editTiempoExperiencia, editEmail, editPassword;
    private Button bttGuardar, bttEliminar;
    private Maestro maestro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_maestro);

        // Inicializar vistas
        editNombre = findViewById(R.id.editNombre);
        editPhone = findViewById(R.id.editPhone);
        editSexo = findViewById(R.id.editSexo);
        editEdad = findViewById(R.id.editEdad);
        editExperiencia = findViewById(R.id.editExperiencia);
        editTiempoExperiencia = findViewById(R.id.editTiempoExperiencia);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        bttGuardar = findViewById(R.id.btt_guardar);
        bttEliminar = findViewById(R.id.btt_eliminar);

        // Recuperar el id del maestro desde SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        int idMaestro = sharedPreferences.getInt("idMaestro", -1);

        if (idMaestro == -1) {
            Toast.makeText(this, "Maestro no encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Obtener los detalles del maestro desde el servidor
        new GetMaestroDetailsTask(idMaestro).execute();

        // Inicializar maestro
        maestro = new Maestro();
        maestro.setId(idMaestro);

        // Configurar botones
        bttGuardar.setOnClickListener(v -> modificarMaestro());
        bttEliminar.setOnClickListener(v -> eliminarMaestro());
    }

    private void modificarMaestro() {
        // Actualizar datos del maestro con los valores de los campos
        maestro.setNombre(editNombre.getText().toString());
        maestro.setTelefono(editPhone.getText().toString());
        maestro.setSexo(editSexo.getText().toString());
        maestro.setEdad(editEdad.getText().toString());
        maestro.setExperiencia(editExperiencia.getText().toString());
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            maestro.setTiempoCampo(sdf.parse(editTiempoExperiencia.getText().toString()));
        } catch (ParseException e) {
            Toast.makeText(this, "Formato de fecha inválido (yyyy-MM-dd)", Toast.LENGTH_SHORT).show();
            return;
        }
        maestro.setCorreo(editEmail.getText().toString());
        maestro.setClave(editPassword.getText().toString());

        new UpdateMaestroTask(maestro).execute();
    }

    private void eliminarMaestro() {
        new DeleteMaestroTask(maestro.getId()).execute();
    }

    private class UpdateMaestroTask extends AsyncTask<Void, Void, String> {
        private final Maestro maestro;

        public UpdateMaestroTask(Maestro maestro) {
            this.maestro = maestro;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL("https://ms-maestros-1078682117753.us-central1.run.app/v1/update-maestro/" + maestro.getId());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PUT");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                JSONObject json = new JSONObject();
                json.put("idCategoria", maestro.getIdCategoria());
                json.put("nombre", maestro.getNombre());
                json.put("telefono", maestro.getTelefono());
                json.put("edad", maestro.getEdad());
                json.put("sexo", maestro.getSexo());
                json.put("experiencia", maestro.getExperiencia());
                json.put("tiempoCampo", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(maestro.getTiempoCampo()));
                json.put("correo", maestro.getCorreo());
                json.put("clave", maestro.getClave());

                try (DataOutputStream os = new DataOutputStream(connection.getOutputStream())) {
                    os.writeBytes(json.toString());
                    os.flush();
                }

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    return "Maestro actualizado exitosamente";
                } else {
                    return "Error al actualizar: " + connection.getResponseCode();
                }
            } catch (Exception e) {
                return "Error: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(EditarMaestro.this, result, Toast.LENGTH_SHORT).show();
        }
    }

    private class DeleteMaestroTask extends AsyncTask<Void, Void, String> {
        private final int idMaestro;

        public DeleteMaestroTask(int idMaestro) {
            this.idMaestro = idMaestro;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL("https://ms-maestros-1078682117753.us-central1.run.app/v1/delete-maestro/" + idMaestro);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("DELETE");

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    return "Maestro eliminado exitosamente";
                } else {
                    return "Error al eliminar: " + connection.getResponseCode();
                }
            } catch (Exception e) {
                return "Error: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(EditarMaestro.this, result, Toast.LENGTH_SHORT).show();
            if (result.contains("eliminado")) {
                finish();
            }
        }
    }

    // Clase GetMaestroDetailsTask fuera de DeleteMaestroTask
    private class GetMaestroDetailsTask extends AsyncTask<Void, Void, Maestro> {
        private final int idMaestro;

        public GetMaestroDetailsTask(int idMaestro) {
            this.idMaestro = idMaestro;
        }

        @Override
        protected Maestro doInBackground(Void... voids) {
            Maestro maestro = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL("https://ms-maestros-1078682117753.us-central1.run.app/v1/maestros/" + idMaestro);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000); // Timeout for connection
                connection.setReadTimeout(5000); // Timeout for reading response

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Code for reading the response from the server
                } else {
                    // Log error for failed response
                    Log.e("GetMaestroDetails", "Error: Response code " + responseCode);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("GetMaestroDetails", "Exception: " + e.getMessage());
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return maestro;
        }


        @Override
        protected void onPostExecute(Maestro maestro) {
            super.onPostExecute(maestro);
            if (maestro != null) {
                // Poblar los campos de edición con los datos obtenidos
                editNombre.setText(maestro.getNombre());
                editPhone.setText(maestro.getTelefono());
                editSexo.setText(maestro.getSexo());
                editEdad.setText(maestro.getEdad());
                editExperiencia.setText(maestro.getExperiencia());
                editTiempoExperiencia.setText(new SimpleDateFormat("yyyy-MM-dd").format(maestro.getTiempoCampo()));
                editEmail.setText(maestro.getCorreo());
                editPassword.setText(maestro.getClave());
            } else {
                Toast.makeText(EditarMaestro.this, "Error al recuperar los datos", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

