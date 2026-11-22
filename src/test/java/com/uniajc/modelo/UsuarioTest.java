package com.uniajc.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias para el modelo Usuario.
 */
public class UsuarioTest {

    @Test
    @DisplayName("El constructor tradicional conserva todos los datos del usuario")
    void constructorConservaLosDatos() {
        //  Se crea un objeto Usuario con todos sus atributos
        Usuario u = new Usuario("10", "admin", "Lauren", "Valderrama");

        assertEquals("10", u.getId());
        assertEquals("admin", u.getUsername());
        assertEquals("Lauren", u.getNombres());
        assertEquals("Valderrama", u.getApellidos());
        assertEquals("USUARIO", u.getRol());
        assertTrue(u.isActivo());
    }

    @Test
    @DisplayName("El constructor completo según practica-mvc.usuarios asigna correctamente los atributos")
    void constructorCompleto() {
        //  Se crea un objeto Usuario con todos sus atributos
        Usuario u = new Usuario("1", "jdoe", "John Doe", "jdoe@uniajc.edu.co", "ADMIN", true);
        //  Se valida que los atributos hayan sido correctamente asignados
        assertEquals("1", u.getId());
        assertEquals("jdoe", u.getUsername());
        assertEquals("jdoe", u.getUsuario());
        assertEquals("John Doe", u.getNombreCompleto());
        assertEquals("jdoe@uniajc.edu.co", u.getCorreo());
        assertEquals("ADMIN", u.getRol());
        assertTrue(u.isActivo());
    }

    @Test
    @DisplayName("getNombreCompleto une nombres y apellidos con un espacio si ambos existen")
    void nombreCompleto() {
        //  Se crea un objeto Usuario con nombres y apellidos
        Usuario u = new Usuario("1", "lvalderrama", "Lauren Sofia", "Valderrama Badillo");

        assertEquals("Lauren Sofia Valderrama Badillo", u.getNombreCompleto());
    }

    @Test
    @DisplayName("getNombreCompleto devuelve solo nombres si apellidos es nulo o vacío")
    void nombreCompletoSinApellidos() {
        //  Se crea un objeto Usuario con nombres y sin apellidos
        Usuario u = new Usuario("2", "carlos", "Carlos Andrés Pérez", "", "carlos@uniajc.edu.co", "USUARIO", true);

        assertEquals("Carlos Andrés Pérez", u.getNombreCompleto());
    }
}
