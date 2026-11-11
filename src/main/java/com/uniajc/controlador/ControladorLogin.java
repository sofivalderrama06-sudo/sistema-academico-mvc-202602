package com.uniajc.controlador;

import java.util.Optional;

import com.uniajc.dao.UsuarioDao;
import com.uniajc.modelo.Usuario;
import com.uniajc.servicios.UsuarioService;

/**
 * Controlador del login: conecta la vista con el servicio.
 * No muestra nada: eso le toca a la vista.
 */
public class ControladorLogin {

    private final UsuarioService usuarioService;

    public ControladorLogin() {
        this.usuarioService = new UsuarioService(new UsuarioDao());
    }

    public Optional<Usuario> iniciarSesion(String username, String password) {
        return usuarioService.iniciarSesion(username, password);
    }
}