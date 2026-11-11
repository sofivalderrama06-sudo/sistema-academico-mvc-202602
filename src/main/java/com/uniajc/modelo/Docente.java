package com.uniajc.modelo;

/**
 * Modelo: representa un docente. No imprime nada ni habla con la base de datos.
 * En la tabla "practica-mvc".docentes las columnas se llaman
 * id, name, specialty y email (el DAO se encarga de mapearlas).
 */
public class Docente {

    private int id;
    private String nombre;
    private String especialidad;
    private String email;

    public Docente() {
    }

    public Docente(int id, String nombre, String especialidad, String email) {
        this.id = id;
        this.nombre = nombre;
        this.especialidad = especialidad;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return id + " | " + nombre + " " + especialidad + " | " + email;
    }
}