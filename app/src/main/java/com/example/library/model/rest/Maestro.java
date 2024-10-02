package com.example.library.model.rest;

public class Maestro {
    private int id_maestro;
    private String nombre;
    private String telefono;
    private int edad;
    private String sexo;
    private String experiencia;
    private String tiempo_campo; // Considera usar Date si es necesario
    private String especialidad;
    private String url_imagen; // Nueva columna

    public Maestro(int id_maestro, String nombre, String telefono, int edad,
                   String sexo, String experiencia, String tiempo_campo, String especialidad,
                   String url_imagen) {
        this.id_maestro = id_maestro;
        this.nombre = nombre;
        this.telefono = telefono;
        this.edad = edad;
        this.sexo = sexo;
        this.experiencia = experiencia;
        this.tiempo_campo = tiempo_campo;
        this.especialidad = especialidad;
        this.url_imagen = url_imagen;
    }

    public Maestro() {
    }

    public int getId_maestro() {
        return id_maestro;
    }

    public void setId_maestro(int id_maestro) {
        this.id_maestro = id_maestro;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public int getEdad() {
        return edad;
    }

    public void setEdad(int edad) {
        this.edad = edad;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public String getExperiencia() {
        return experiencia;
    }

    public void setExperiencia(String experiencia) {
        this.experiencia = experiencia;
    }

    public String getTiempo_campo() {
        return tiempo_campo;
    }

    public void setTiempo_campo(String tiempo_campo) {
        this.tiempo_campo = tiempo_campo;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public String getUrl_imagen() {
        return url_imagen;
    }

    public void setUrl_imagen(String url_imagen) {
        this.url_imagen = url_imagen;
    }
}