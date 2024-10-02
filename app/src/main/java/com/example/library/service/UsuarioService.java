package com.example.library.service;

import com.example.library.model.rest.Usuario;
import com.google.gson.Gson;
import okhttp3.*;
import java.io.IOException;

public class UsuarioService {
    private static final String BASE_URL = "http://localhost/app-mobile-master/usuarios_api.php"; // Cambia a localhost si estás en un dispositivo real
    private final OkHttpClient client;
    private final Gson gson;

    public UsuarioService() {
        this.client = new OkHttpClient();
        this.gson = new Gson();
    }

    // Método para buscar un usuario por correo y clave
    public Usuario buscarUsuarioPorCorreoYClave(String correo, String clave) throws IOException {
        String url = BASE_URL + "?correo=" + correo + "&clave=" + clave;
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return gson.fromJson(response.body().string(), Usuario.class);
            } else {
                throw new IOException("Error en la petición: " + response.code());
            }
        }
    }

    // Método para insertar un nuevo usuario
    public void insertarUsuario(Usuario usuario) throws IOException {
        RequestBody body = RequestBody.create(gson.toJson(usuario), MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(BASE_URL)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Error en la petición: " + response.code());
            }
        }
    }

    // Método para actualizar un usuario por ID
    public void actualizarUsuarioPorId(int id, Usuario usuario) throws IOException {
        RequestBody body = RequestBody.create(gson.toJson(usuario), MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(BASE_URL + "/" + id)
                .put(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Error en la petición: " + response.code());
            }
        }
    }

    // Método para eliminar un usuario por ID
    public void eliminarUsuario(int id) throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL + "/" + id)
                .delete()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Error en la petición: " + response.code());
            }
        }
    }
}
