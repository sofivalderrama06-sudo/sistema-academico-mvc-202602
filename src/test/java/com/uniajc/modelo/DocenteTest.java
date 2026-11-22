package com.uniajc.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * PRUEBAS UNITARIAS: Capa de Modelo (DocenteTest)
 * ¿Qué se prueba en la capa de Modelo?
 * - Se prueban los objetos POJO (Plain Old Java Objects) en Java puro.
 * - No requiere conexión a base de datos, DAO ni Servicios.
 * - Verifica que los constructores asignen los atributos correctamente,
 *   que los setters modifiquen los estados esperados y que los métodos de
 *   representación textual como 'toString()' entreguen el formato requerido.
 */
public class DocenteTest {

    @Test
    @DisplayName("El constructor vacio deja los campos sin valor")
    void constructorVacio() {
        // Se instancia el objeto sin parámetros
        Docente d = new Docente();

        //  Se verifica que los valores por defecto sean coherentes (0 para enteros, null para objetos)
        assertEquals(0, d.getId());
        assertNull(d.getNombre());
        assertNull(d.getEspecialidad());
        assertNull(d.getEmail());
    }

    @Test
    @DisplayName("El constructor completo asigna los cuatro campos")
    void constructorCompleto() {
        //  Se instancia el docente con sus 4 atributos
        Docente d = new Docente(2, "Marta Rios", "Matematicas", "marta@uni.edu.co");

        //  Se valida que cada getter retorne exactamente lo enviado al constructor
        assertEquals(2, d.getId());
        assertEquals("Marta Rios", d.getNombre());
        assertEquals("Matematicas", d.getEspecialidad());
        assertEquals("marta@uni.edu.co", d.getEmail());
    }

    @Test
    @DisplayName("Los setters modifican los campos correctamente")
    void setters() {
        //  Se crea un docente vacío
        Docente d = new Docente();

        // Se modifican sus atributos mediante los métodos setters
        d.setId(5);
        d.setNombre("Luis Mina");
        d.setEspecialidad("Fisica");
        d.setEmail("luis@uni.edu.co");

        // Se verifica que el estado interno del objeto haya cambiado
        assertEquals(5, d.getId());
        assertEquals("Luis Mina", d.getNombre());
        assertEquals("Fisica", d.getEspecialidad());
        assertEquals("luis@uni.edu.co", d.getEmail());
    }

    @Test
    @DisplayName("toString muestra id, nombre, especialidad y correo")
    void textoDelObjeto() {
        Docente d = new Docente(1, "Carlos Gomez", "Sistemas", "carlos@uni.edu.co");
        String texto = d.toString();

        // Verifica que la representación en cadena contenga los datos esenciales
        assertEquals("1 | Carlos Gomez | Sistemas | carlos@uni.edu.co", texto);
    }
}
