package com.uniajc.servicios;

import java.util.List;
import java.util.Optional;

import com.uniajc.dao.UsuarioDao;
import com.uniajc.modelo.Usuario;
import com.uniajc.util.PasswordUtil;

/**
 * Servicio: lógica de negocio y validaciones para el módulo de usuarios.
 * Aplica el cifrado BCrypt antes de persistir cualquier credencial.
 */
public class UsuarioService {

    private static final List<String> ROLES_VALIDOS = List.of("ADMIN", "DOCENTE", "ESTUDIANTE", "USUARIO");

    private final UsuarioDao usuarioDao;

    public UsuarioService(UsuarioDao usuarioDao) {
        this.usuarioDao = usuarioDao;
    }

    /**
     * Inicia sesión validando credenciales contra el hash BCrypt almacenado.
     */
    public Optional<Usuario> iniciarSesion(String username, String password) {
        String usuario = username == null ? "" : username.trim();
        String clave = password == null ? "" : password;

        if (usuario.isEmpty()) {
            throw new IllegalArgumentException("El usuario es obligatorio.");
        }
        if (clave.isEmpty()) {
            throw new IllegalArgumentException("La contrasena es obligatoria.");
        }
        return usuarioDao.autenticar(usuario, clave);
    }

    /**
     * Obtiene la lista completa de usuarios.
     */
    public List<Usuario> obtenerTodosLosUsuarios() {
        return usuarioDao.obtenerTodos();
    }

    /**
     * Busca un usuario por ID.
     */
    public Optional<Usuario> buscarUsuarioPorId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID inválido.");
        }
        return usuarioDao.buscarPorId(id);
    }

    /**
     * Registra un nuevo usuario aplicando BCrypt a la contraseña.
     */
    public void registrarUsuario(Usuario usuario, String passwordPlano) {
        validarUsuario(usuario);

        if (passwordPlano == null || passwordPlano.trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña es obligatoria.");
        }
        if (passwordPlano.length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres.");
        }

        String passwordHasheado = PasswordUtil.hashear(passwordPlano);
        usuarioDao.guardar(usuario, passwordHasheado);
    }

    /**
     * Actualiza un usuario existente. Si nuevaClave no está vacía, se hashea con BCrypt.
     */
    public void actualizarUsuario(Usuario usuario, String nuevaClave) {
        if (usuario == null || usuario.getId() == null || usuario.getId().isBlank()) {
            throw new IllegalArgumentException("Usuario inválido para actualizar.");
        }
        validarUsuario(usuario);

        String nuevaPasswordHasheada = null;
        if (nuevaClave != null && !nuevaClave.trim().isEmpty()) {
            if (nuevaClave.length() < 6) {
                throw new IllegalArgumentException("La nueva contraseña debe tener al menos 6 caracteres.");
            }
            nuevaPasswordHasheada = PasswordUtil.hashear(nuevaClave);
        }

        usuarioDao.actualizar(usuario, nuevaPasswordHasheada);
    }

    /**
     * Elimina un usuario por su ID.
     */
    public void eliminarUsuario(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID inválido.");
        }
        usuarioDao.eliminar(id);
    }

    private void validarUsuario(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("Los datos del usuario son obligatorios.");
        }
        if (usuario.getUsername() == null || usuario.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("El usuario es obligatorio.");
        }
        if (usuario.getNombreCompleto() == null || usuario.getNombreCompleto().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre completo es obligatorio.");
        }
        if (usuario.getCorreo() == null || !usuario.getCorreo().contains("@")) {
            throw new IllegalArgumentException("El correo electrónico no es válido.");
        }
        
        String rol = usuario.getRol() == null ? "" : usuario.getRol().trim().toUpperCase();
        if (!ROLES_VALIDOS.contains(rol)) {
            throw new IllegalArgumentException("El rol debe ser ADMIN, DOCENTE, ESTUDIANTE o USUARIO.");
        }
    }
}
