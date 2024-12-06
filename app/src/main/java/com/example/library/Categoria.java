package com.example.library;

public class Categoria {
    private int idCategoria;
    private String especialidad;

    public Categoria(int idCategoria, String especialidad) {
        this.idCategoria = idCategoria;
        this.especialidad = especialidad;
    }

    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    @Override
    public String toString() {
        return especialidad;
    }
}

