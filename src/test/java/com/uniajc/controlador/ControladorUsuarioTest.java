package com.uniajc.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.uniajc.modelo.Usuario;
import com.uniajc.soporte.BdFalsa;

/**
 * Pruebas unitarias para ControladorUsuario.
 * Valida el flujo completo Controlador -> Servicio -> DAO sobre BdFalsa.
 */
public class ControladorUsuarioTest {

    private BdFalsa bd;
    private ControladorUsuario controlador;

    @BeforeEach
    void preparar() {
        bd = new BdFalsa().instalar();
        controlador = new ControladorUsuario();
    }

    @AfterEach
    void limpiar() {
        BdFalsa.desinstalar();
    }

    @Test
    @DisplayName("registrar usuario ejecuta el INSERT con los parámetros encriptados")
    void registrar() {
        controlador.registrar("laura", "claveSegura123", "Laura Gómez", "laura@uniajc.edu.co", "USUARIO");

        assertEquals(1, bd.sqls.size());
        assertTrue(bd.sqls.get(0).startsWith("INSERT INTO \"practica-mvc\".usuarios"));
        assertEquals("laura", bd.ultimosParametros().get(1));
        
        // Verifica que la contraseña enviada a la BD sea un hash BCrypt y no texto plano
        String passEnviada = (String) bd.ultimosParametros().get(2);
        assertTrue(passEnviada.startsWith("$2a$12$"), "La contraseña debe estar hasheada con BCrypt");
        assertEquals("Laura Gómez", bd.ultimosParametros().get(3));
        assertEquals("laura@uniajc.edu.co", bd.ultimosParametros().get(4));
        assertEquals("USUARIO", bd.ultimosParametros().get(5));
    }

    @Test
    @DisplayName("registrar con contraseña corta lanza error antes de tocar la base de datos")
    void registrarPasswordCorta() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controlador.registrar("laura", "123", "Laura Gómez", "laura@uniajc.edu.co", "USUARIO"));

        assertEquals("La contraseña debe tener al menos 6 caracteres.", ex.getMessage());
        assertTrue(bd.sqls.isEmpty(), "No debe ejecutar ninguna consulta en la base de datos");
    }

    @Test
    @DisplayName("eliminar ejecuta el DELETE en la base de datos")
    void eliminar() {
        controlador.eliminar(5);

        assertEquals(1, bd.sqls.size());
        assertTrue(bd.sqls.get(0).startsWith("DELETE FROM \"practica-mvc\".usuarios"));
        assertEquals(5, bd.ultimosParametros().get(1));
    }
}
