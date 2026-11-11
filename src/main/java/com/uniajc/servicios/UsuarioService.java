package com.uniajc.servicios;

import java.util.Optional;

import com.uniajc.dao.UsuarioDao;
import com.uniajc.modelo.Usuario;

/**
 * Servicio: reglas del login. Valida los datos antes de ir a la base de datos.
 */
public class UsuarioService {

    private final UsuarioDao usuarioDao;

    public UsuarioService(UsuarioDao usuarioDao) {
        this.usuarioDao = usuarioDao;
    }

    /**
     * @return el usuario si las credenciales son correctas; vacio si no lo son.
     * @throws IllegalArgumentException si falta el usuario o la contrasena.
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
}