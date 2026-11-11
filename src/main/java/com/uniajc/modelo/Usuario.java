package com.uniajc.modelo;

/**
 * Modelo: usuario que inicio sesion. Nunca guarda la contrasena.
 * Los datos vienen de academico.usuarios + academico.personas.
 */
public class Usuario {

    private final String id;
    private final String username;
    private final String nombres;
    private final String apellidos;

    public Usuario(String id, String username, String nombres, String apellidos) {
        this.id = id;
        this.username = username;
        this.nombres = nombres;
        this.apellidos = apellidos;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getNombres() {
        return nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }
}