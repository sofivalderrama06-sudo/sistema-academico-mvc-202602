package com.uniajc.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.sql.Timestamp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class GrupoTest {

    @Test
    @DisplayName("El constructor sin id deja el id en 0 (grupo nuevo)")
    void constructorSinId() {
        //  Se crea un grupo sin id, simulando un nuevo registro
        Grupo g = new Grupo(2, 3, "Aula 101", "Lunes 8-10", null);
//  Se valida que los atributos hayan sido correctamente asignados
        assertEquals(0, g.getId());
        assertEquals(2, g.getId_materia());
        assertEquals(3, g.getId_docente());
        assertEquals("Aula 101", g.getClassroom());
        assertEquals("Lunes 8-10", g.getSchedule());
        assertNull(g.getCreated_at());
    }

    @Test
    @DisplayName("El constructor con id reconstruye un grupo existente")
    void constructorConId() {
        //  Se crea un grupo con id, simulando un registro existente
        Timestamp fecha = new Timestamp(0L);
        Grupo g = new Grupo(9, 2, 3, "Aula 202", "Martes 10-12", fecha);

        assertEquals(9, g.getId());
        assertEquals(2, g.getId_materia());
        assertEquals(3, g.getId_docente());
        assertEquals("Aula 202", g.getClassroom());
        assertEquals("Martes 10-12", g.getSchedule());
        assertSame(fecha, g.getCreated_at());
    }

    @Test
    @DisplayName("Los setters modifican los campos")
    void setters() {
        //  Se crea un grupo con valores iniciales
        Grupo g = new Grupo(1, 1, "A", "B", null);
        Timestamp fecha = new Timestamp(1000L);
//  Se modifican los atributos mediante los métodos setters
        g.setId(5);
        g.setId_materia(6);
        g.setId_docente(7);
        g.setClassroom("Laboratorio");
        g.setSchedule("Viernes 2-4");
        g.setCreated_at(fecha);
//  Se verifica que los valores hayan sido correctamente asignados
        assertEquals(5, g.getId());
        assertEquals(6, g.getId_materia());
        assertEquals(7, g.getId_docente());
        assertEquals("Laboratorio", g.getClassroom());
        assertEquals("Viernes 2-4", g.getSchedule());
        assertSame(fecha, g.getCreated_at());
    }
}
