package com.example.library.model.rest;


public class Reserva {
    private int id_reserva;
    private int id_maestro; // Foreign key
    private String fecha_visita; // Considera usar Date si es necesario
    private double coste;
    private String ciudad;
    private int id_usuario; // Foreign key

    public Reserva() {
    }

    public Reserva(int id_reserva, int id_maestro, String fecha_visita, double coste,
                   String ciudad, int id_usuario) {
        this.id_reserva = id_reserva;
        this.id_maestro = id_maestro;
        this.fecha_visita = fecha_visita;
        this.coste = coste;
        this.ciudad = ciudad;
        this.id_usuario = id_usuario;
    }


    public int getId_reserva() {
        return id_reserva;
    }

    public void setId_reserva(int id_reserva) {
        this.id_reserva = id_reserva;
    }

    public int getId_maestro() {
        return id_maestro;
    }

    public void setId_maestro(int id_maestro) {
        this.id_maestro = id_maestro;
    }

    public String getFecha_visita() {
        return fecha_visita;
    }

    public void setFecha_visita(String fecha_visita) {
        this.fecha_visita = fecha_visita;
    }

    public double getCoste() {
        return coste;
    }

    public void setCoste(double coste) {
        this.coste = coste;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public int getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(int id_usuario) {
        this.id_usuario = id_usuario;
    }
}