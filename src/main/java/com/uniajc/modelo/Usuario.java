package com.uniajc.modelo;

/**
 * Modelo: Representa un usuario del sistema (tabla "practica-mvc".usuarios).
 * Por seguridad, nunca retiene la contraseña en memoria después de autenticado.
 */
public class Usuario {

    private final String id;
    private final String usuario; // Nombre de usuario en el sistema
    private final String nombres;
    private final String apellidos;
    private final String correo;
    private final String rol;       // ADMIN, DOCENTE, ESTUDIANTE 
    private final boolean activo;

    /**
     * Constructor original para mantener compatibilidad con vistas y pruebas existentes.
     */
    public Usuario(String id, String usuario, String nombres, String apellidos) {
        this(id, usuario, nombres, apellidos, "", "USUARIO", true);
    }

    /**
     * Constructor completo para la entidad según "practica-mvc".usuarios.
     */
    public Usuario(String id, String usuario, String nombreCompleto, String correo, String rol, boolean activo) {
        this(id, usuario, nombreCompleto, "", correo, rol, activo);
    }

    public Usuario(String id, String usuario, String nombres, String apellidos, String correo, String rol, boolean activo) {
        this.id = id;
        this.usuario = usuario;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.correo = correo;
        this.rol = rol;
        this.activo = activo;
    }

    public String getId() {
        return id;
    }

    /**
     * Retorna el identificador de inicio de sesión (nombre de usuario).
     */
    public String getUsername() {
        return usuario;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getNombres() {
        return nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public String getCorreo() {
        return correo;
    }

    public String getRol() {
        return rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public String getNombreCompleto() {
        if (apellidos == null || apellidos.trim().isEmpty()) {
            return nombres != null ? nombres.trim() : "";
        }
        return (nombres + " " + apellidos).trim();
    }
}
