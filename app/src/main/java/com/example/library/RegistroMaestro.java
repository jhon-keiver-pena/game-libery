package com.example.library;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.gamelibery.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsCompat.Type;
import okhttp3.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegistroMaestro extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView ivSelectedImage;
    private Uri imageUri;
    private String selectedCategoryId; // ID de la categoría seleccionada

    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_maestro);

        executorService = Executors.newSingleThreadExecutor();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btnSelectImage = findViewById(R.id.btn_select_image);
        Button btnCrearMaestro = findViewById(R.id.btn_crear_maestro);
        Button btnIrLogin = findViewById(R.id.btn_ir_inicio);
        Spinner spinner = findViewById(R.id.spinner);
        ivSelectedImage = findViewById(R.id.iv_selected_image);

        String[] categorias = {"Electricista", "Plomero", "Carpintero", "Albañil", "Pintor", "Soldador", "Jardinero", "Mecánico"};

        // Mapeo de categorías a IDs
        HashMap<String, String> categoriaMap = new HashMap<>();
        categoriaMap.put("Electricista", "1");
        categoriaMap.put("Plomero", "2");
        categoriaMap.put("Carpintero", "3");
        categoriaMap.put("Albañil", "4");
        categoriaMap.put("Pintor", "5");
        categoriaMap.put("Soldador", "6");
        categoriaMap.put("Jardinero", "7");
        categoriaMap.put("Mecánico", "8");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categorias);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Asignar el ID de la categoría seleccionada al cambiar el Spinner
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCategory = categorias[position];
                selectedCategoryId = categoriaMap.get(selectedCategory);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCategoryId = null; // Ninguna categoría seleccionada
            }
        });

        btnSelectImage.setOnClickListener(v -> openFileChooser());
        btnCrearMaestro.setOnClickListener(v -> sendFormData());
        btnIrLogin.setOnClickListener(v -> goLogin());
    }

    private void goLogin() {
        Intent intent = new Intent(getBaseContext(), Login.class);
        startActivity(intent);
        finish();
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
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
        EditText etName = findViewById(R.id.editNombre);
        EditText etPhone = findViewById(R.id.editTlfMaestro);
        EditText etSexo = findViewById(R.id.editSexo);
        EditText etEdad = findViewById(R.id.editEdad);
        EditText etExperience = findViewById(R.id.editExperienciaCamp);
        EditText etTiempoCampo = findViewById(R.id.editDate);
        EditText etEmail = findViewById(R.id.editCorreoMaestro);
        EditText etPass = findViewById(R.id.editClaveMaestro);

        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String sexo = etSexo.getText().toString().trim().toUpperCase();
        String edadStr = etEdad.getText().toString().trim();
        String experience = etExperience.getText().toString().trim();
        String tiempoCampo = etTiempoCampo.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String pass = etPass.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Por favor complete todos los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        int edad;
        try {
            edad = Integer.parseInt(edadStr);
            if (edad <= 0) {
                Toast.makeText(this, "La edad debe ser mayor a 0", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "La edad debe ser un número válido", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!sexo.equals("M") && !sexo.equals("F")) {
            Toast.makeText(this, "El sexo debe ser 'M' o 'F'", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!tiempoCampo.matches("\\d{4}-\\d{2}-\\d{2}")) {
            Toast.makeText(this, "La fecha debe estar en formato yyyy-MM-dd", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedCategoryId == null) {
            Toast.makeText(this, "Por favor seleccione una categoría", Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            try {
                OkHttpClient client = new OkHttpClient();

                MultipartBody.Builder bodyBuilder = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("id_categoria", selectedCategoryId)
                        .addFormDataPart("nombre", name)
                        .addFormDataPart("telefono", phone)
                        .addFormDataPart("edad", String.valueOf(edad))
                        .addFormDataPart("sexo", sexo)
                        .addFormDataPart("experiencia", experience)
                        .addFormDataPart("tiempo_campo", tiempoCampo)
                        .addFormDataPart("correo", email)
                        .addFormDataPart("clave", pass);

                if (imageUri != null) {
                    InputStream inputStream = getContentResolver().openInputStream(imageUri);
                    byte[] imageBytes = getBytes(inputStream);
                    RequestBody imageRequestBody = RequestBody.create(MediaType.parse("image/*"), imageBytes);
                    bodyBuilder.addFormDataPart("image", "image.jpg", imageRequestBody);
                }

                RequestBody requestBody = bodyBuilder.build();

                Request request = new Request.Builder()
                        .url("https://ms-maestros-1078682117753.us-central1.run.app/v1/insert-maestro")
                        .post(requestBody)
                        .build();

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

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, length);
        }
        return byteArrayOutputStream.toByteArray();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}

