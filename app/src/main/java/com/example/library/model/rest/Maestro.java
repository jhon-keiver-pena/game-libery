package com.example.library.model.rest;


public class Maestro {
    private int id;
    private String nombre;
    private String telefono;
    private String edad;
    private String sexo;
    private String experiencia;
    private String tiempoCampo;
    private String especialidad;
    private String urlImagen;

    // Constructor, getters y setters
    public Maestro(int id, String nombre, String telefono, String edad, String sexo,
                   String experiencia, String tiempoCampo, String especialidad, String urlImagen) {
        this.id = id;
        this.nombre = nombre;
        this.telefono = telefono;
        this.edad = edad;
        this.sexo = sexo;
        this.experiencia = experiencia;
        this.tiempoCampo = tiempoCampo;
        this.especialidad = especialidad;
        this.urlImagen = urlImagen;
    }

    // Getters
    public String getNombre() {
        return nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getEdad() {
        return edad;
    }

    public String getSexo() {
        return sexo;
    }

    public String getExperiencia() {
        return experiencia;
    }

    public String getTiempoCampo() {
        return tiempoCampo;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public String getUrlImagen() {
        return urlImagen;
    }
}
