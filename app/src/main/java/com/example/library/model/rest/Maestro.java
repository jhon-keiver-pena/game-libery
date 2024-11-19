package com.example.library.model.rest;


import java.io.Serializable;
import java.util.Date;

public class Maestro  implements Serializable {
    private int id;
    private int idCategoria;
    private String nombreCategoria;
    private String nombre;
    private String telefono;
    private String edad;
    private String sexo;
    private String experiencia;
    private Date tiempoCampo;
    private String correo;
    private String clave;
    private byte[] image;

    // Constructor, getters y setters
    public Maestro(int id, int idCategoria, String nombre, String telefono, String edad, String sexo,
                   String experiencia, Date tiempoCampo, String correo, String clave, byte[] image) {
        this.id = id;
        this.idCategoria = idCategoria;
        this.nombre = nombre;
        this.telefono = telefono;
        this.edad = edad;
        this.sexo = sexo;
        this.experiencia = experiencia;
        this.tiempoCampo = tiempoCampo;
        this.correo = correo;
        this.clave = clave;
        this.image = image;
    }

    public Maestro() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNombreCategoria() {
        return nombreCategoria;
    }

    public void setNombreCategoria(String nombreCategoria) {
        this.nombreCategoria = nombreCategoria;
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

    public String getEdad() {
        return edad;
    }

    public void setEdad(String edad) {
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

    public Date getTiempoCampo() {
        return tiempoCampo;
    }

    public void setTiempoCampo(Date tiempoCampo) {
        this.tiempoCampo = tiempoCampo;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }
}