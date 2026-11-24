package com.uniajc.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Timestamp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * ============================================================================
 * PRUEBAS UNITARIAS: Modelo Inscripcion (POJO)
 * ============================================================================
 * ¿Qué validamos aquí?
 * 1. Constructor vacío: Permite instanciar el objeto e inicializarlo con setters.
 * 2. Constructor sin ID: Simula la creación previa a persistir en BD (ID = 0, created_at = null).
 * 3. Constructor completo: Reconstruye un registro recuperado de PostgreSQL con su ID y timestamp.
 * 4. Getters y Setters: Garantizan el correcto encapsulamiento y mutabilidad controlada.
 * 5. Método toString(): Representación amigable para interfaces gráficas y logs de depuración.
 */
public class InscripcionTest {

    @Test
    @DisplayName("El constructor vacío permite asignar y consultar valores con getters y setters")
    void constructorVacioYSetters() {
        Inscripcion inscripcion = new Inscripcion();
        Timestamp ahora = new Timestamp(System.currentTimeMillis());

        inscripcion.setId(10);
        inscripcion.setId_estudiante(3);
        inscripcion.setId_grupo(7);
        inscripcion.setFinal_grade(4.5);
        inscripcion.setStatus("ACTIVO");
        inscripcion.setCreated_at(ahora);

        assertEquals(10, inscripcion.getId());
        assertEquals(3, inscripcion.getId_estudiante());
        assertEquals(7, inscripcion.getId_grupo());
        assertEquals(4.5, inscripcion.getFinal_grade(), 0.001);
        assertEquals("ACTIVO", inscripcion.getStatus());
        assertSame(ahora, inscripcion.getCreated_at());
    }

    @Test
    @DisplayName("El constructor sin ID inicializa con ID en 0 y created_at nulo (registro nuevo)")
    void constructorSinId() {
        // Arrange & Act: Se simula la captura del formulario antes de guardar en BD
        Inscripcion inscripcion = new Inscripcion(5, 8, 3.8, "ACTIVO");

        // Assert: El ID es 0 porque será generado por la secuencia SERIAL de PostgreSQL
        assertEquals(0, inscripcion.getId());
        assertEquals(5, inscripcion.getId_estudiante());
        assertEquals(8, inscripcion.getId_grupo());
        assertEquals(3.8, inscripcion.getFinal_grade(), 0.001);
        assertEquals("ACTIVO", inscripcion.getStatus());
        assertNull(inscripcion.getCreated_at());
    }

    @Test
    @DisplayName("El constructor con ID reconstruye una inscripción existente de la base de datos")
    void constructorConId() {
        Timestamp fecha = new Timestamp(1000000000L);
        Inscripcion inscripcion = new Inscripcion(25, 12, 14, 4.2, "APROBADO", fecha);

        assertEquals(25, inscripcion.getId());
        assertEquals(12, inscripcion.getId_estudiante());
        assertEquals(14, inscripcion.getId_grupo());
        assertEquals(4.2, inscripcion.getFinal_grade(), 0.001);
        assertEquals("APROBADO", inscripcion.getStatus());
        assertSame(fecha, inscripcion.getCreated_at());
    }

    @Test
    @DisplayName("Los setters modifican correctamente cada campo de la inscripción")
    void settersModificanCampos() {
        Inscripcion inscripcion = new Inscripcion(1, 1, 0.0, "ACTIVO");
        Timestamp nuevaFecha = new Timestamp(2000000000L);

        inscripcion.setId(99);
        inscripcion.setId_estudiante(20);
        inscripcion.setId_grupo(30);
        inscripcion.setFinal_grade(5.0);
        inscripcion.setStatus("REPROBADO");
        inscripcion.setCreated_at(nuevaFecha);

        assertEquals(99, inscripcion.getId());
        assertEquals(20, inscripcion.getId_estudiante());
        assertEquals(30, inscripcion.getId_grupo());
        assertEquals(5.0, inscripcion.getFinal_grade(), 0.001);
        assertEquals("REPROBADO", inscripcion.getStatus());
        assertSame(nuevaFecha, inscripcion.getCreated_at());
    }

    @Test
    @DisplayName("El método toString devuelve una descripción legible con ID, estudiante y grupo")
    void toStringLegible() {
        Inscripcion inscripcion = new Inscripcion(15, 2, 4, 4.0, "ACTIVO", null);

        String texto = inscripcion.toString();

        assertTrue(texto.contains("15"), "Debe incluir el identificador");
        assertTrue(texto.contains("2"), "Debe incluir el ID del estudiante");
        assertTrue(texto.contains("4"), "Debe incluir el ID del grupo");
    }
}
