package com.uniajc.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.uniajc.modelo.Usuario;
import com.uniajc.soporte.BdFalsa;
import com.uniajc.util.PasswordUtil;

public class ControladorLoginTest {

    private BdFalsa bd;
    private ControladorLogin controlador;

    @BeforeEach
    void preparar() {
        bd = new BdFalsa().instalar();
        controlador = new ControladorLogin();
    }

    @AfterEach
    void limpiar() {
        BdFalsa.desinstalar();
    }

    @Test
    @DisplayName("iniciarSesion devuelve el usuario cuando la base lo encuentra y la clave coincide con BCrypt")
    void loginCorrecto() {
        String hash = PasswordUtil.hashear("1234");
        bd.fila("id", 1, "username", "admin", "password", hash, "nombre", "Lauren Valderrama", "correo", "admin@uniajc.edu.co", "rol", "ADMIN", "activo", true);

        Optional<Usuario> r = controlador.iniciarSesion("admin", "1234");

        assertTrue(r.isPresent());
        assertEquals("Lauren Valderrama", r.get().getNombreCompleto());
        assertEquals("admin", bd.ultimosParametros().get(1));
    }

    @Test
    @DisplayName("iniciarSesion devuelve vacio con credenciales incorrectas")
    void loginIncorrecto() {
        assertFalse(controlador.iniciarSesion("admin", "mala").isPresent());
    }

    @Test
    @DisplayName("iniciarSesion con contrasena que no coincide con el hash devuelve vacio")
    void passwordNoCoincide() {
        String hash = PasswordUtil.hashear("otraClave");
        bd.fila("id", 1, "username", "admin", "password", hash, "nombre", "Lauren Valderrama", "correo", "admin@uniajc.edu.co", "rol", "ADMIN", "activo", true);

        Optional<Usuario> r = controlador.iniciarSesion("admin", "claveIncorrecta");

        assertFalse(r.isPresent());
    }

    @Test
    @DisplayName("iniciarSesion sin usuario o sin contrasena lanza error y no consulta la base")
    void loginIncompleto() {
        IllegalArgumentException usuario = assertThrows(IllegalArgumentException.class,
                () -> controlador.iniciarSesion("", "1234"));
        IllegalArgumentException clave = assertThrows(IllegalArgumentException.class,
                () -> controlador.iniciarSesion("admin", ""));

        assertEquals("El usuario es obligatorio.", usuario.getMessage());
        assertEquals("La contrasena es obligatoria.", clave.getMessage());
        assertTrue(bd.sqls.isEmpty());
    }

    @Test
    @DisplayName("iniciarSesion propaga el error si falla la base de datos")
    void errorDeBaseDeDatos() {
        bd.fallarAlPreparar = true;

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> controlador.iniciarSesion("admin", "1234"));

        assertTrue(ex.getMessage().startsWith("Error al validar el usuario:"));
    }
}
