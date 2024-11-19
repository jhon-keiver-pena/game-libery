package com.example.library;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.gamelibery.R;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegistroMaestro extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView ivSelectedImage;
    private Uri imageUri;
    //Inicio Variables que deben sustituirse por los elementos de la vista
    private String sexo = "M";
    private String edad = "56";
    private String idCategoria = "1";
    //Fin Variables que deben sustituirse por los elementos de la vista
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro_maestro);

        executorService = Executors.newSingleThreadExecutor(); // Executor para manejo de tareas en background

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btnSelectImage = findViewById(R.id.btn_select_image);
        Button btnCrearMaestro = findViewById(R.id.btn_crear_maestro);
        ivSelectedImage = findViewById(R.id.iv_selected_image);

        btnSelectImage.setOnClickListener(v -> openImagePicker());
        btnCrearMaestro.setOnClickListener(v -> sendFormData());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Seleccionar Imagen"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                ivSelectedImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendFormData() {
        EditText etName = findViewById(R.id.editNomMaestro);
        EditText etPhone = findViewById(R.id.editTlfMaestro);
        EditText etExperience = findViewById(R.id.editExperiencia);
        EditText etTiempoCampo = findViewById(R.id.editExperienciaCamp);
        EditText etEmail = findViewById(R.id.editCorreoMaestro);
        EditText etPass = findViewById(R.id.editClaveMaestro);

        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String experience = etExperience.getText().toString().trim();
        String tiempoCampo = etTiempoCampo.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String pass = etPass.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Por favor complete todos los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            try {
                OkHttpClient client = new OkHttpClient();

                // Define the request body using application/x-www-form-urlencoded instead of multipart/form-data
                String formBodyContent =
                        "id_categoria=" + idCategoria +
                        "&nombre=" + name +
                        "&telefono=" + phone +
                        "&edad=" + edad +
                        "&sexo=" + sexo +
                        "&experiencia=" + experience +
                        "&tiempo_campo=" + tiempoCampo +
                        "&correo=" + email +
                        "&clave=" + pass;

                // Agregar la imagen si se seleccionó
                if (imageUri != null) {
                    File file = new File(imageUri.getPath());
                    formBodyContent += "&image=" + file.getName();
                }

                // Crea el request body
                RequestBody requestBody = RequestBody.create(formBodyContent, MediaType.parse("application/x-www-form-urlencoded"));

                Request request = new Request.Builder()
                        .url("http://10.0.2.2:8080/v1/insert-maestro")
                        .post(requestBody)
                        .build();

                // Realiza la solicitud HTTP en un hilo de fondo
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(() -> Toast.makeText(RegistroMaestro.this, "Error al enviar los datos", Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        runOnUiThread(() -> {
                            if (response.isSuccessful()) {
                                Toast.makeText(RegistroMaestro.this, "Datos enviados con éxito", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(RegistroMaestro.this, "Error en el servidor", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(RegistroMaestro.this, "Error al procesar la solicitud", Toast.LENGTH_SHORT).show());
            }
        });
    }
        @Override
        protected void onDestroy() {
            super.onDestroy();
            // Limpiar el ExecutorService
            if (executorService != null && !executorService.isShutdown()) {
                executorService.shutdown();
            }
    }
}
