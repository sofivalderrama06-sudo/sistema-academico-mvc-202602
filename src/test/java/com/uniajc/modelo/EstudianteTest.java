package com.uniajc.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * PRUEBAS UNITARIAS: Capa de Modelo (EstudianteTest)
 * Prueba los comportamientos de la entidad Estudiante:
 * - Constructor vacío y constructor con parámetros.
 * - Correcta asignación y recuperación a través de Getters y Setters.
 * - Formato del método toString().
 * Todo se ejecuta en memoria RAM sin dependencias externas.
 */
public class EstudianteTest {

    @Test
    @DisplayName("El constructor vacio deja los campos sin valor")
    void constructorVacio() {
        //  Se crea un objeto Estudiante vacío
        Estudiante e = new Estudiante();

        assertEquals(0, e.getId());
        assertNull(e.getNombre());
        assertNull(e.getApellido());
        assertNull(e.getEmail());
    }

    @Test
    @DisplayName("El constructor completo asigna los cuatro campos")
    void constructorCompleto() {
        //  Se instancia un objeto Estudiante con todos sus atributos
        Estudiante e = new Estudiante(3, "Ana", "Perez", "ana@uni.edu.co");

        assertEquals(3, e.getId());
        assertEquals("Ana", e.getNombre());
        assertEquals("Perez", e.getApellido());
        assertEquals("ana@uni.edu.co", e.getEmail());
    }

    @Test
    @DisplayName("Los setters modifican los campos")
    void setters() {
        //  Se crea un objeto Estudiante vacío
        Estudiante e = new Estudiante();

        // Se modifican los atributos mediante los métodos setters
        e.setId(7);
        e.setNombre("Juan");
        e.setApellido("Diaz");
        e.setEmail("juan@uni.edu.co");

        // Se verifica que los valores hayan sido correctamente asignados
        assertEquals(7, e.getId());
        assertEquals("Juan", e.getNombre());
        assertEquals("Diaz", e.getApellido());
        assertEquals("juan@uni.edu.co", e.getEmail());
    }

    @Test
    @DisplayName("toString muestra id, nombre completo y correo")
    void textoDelObjeto() {
        Estudiante e = new Estudiante(8, "Sofia", "Lopez", "sofia@uni.edu.co");
        String texto = e.toString();

        assertEquals("8 | Sofia Lopez | sofia@uni.edu.co", texto);
    }
}
