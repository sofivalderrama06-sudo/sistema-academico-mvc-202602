package com.uniajc.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.sql.Timestamp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class MateriaTest {

    @Test
    @DisplayName("El constructor asigna id, nombre, creditos y fecha de creacion")
    void constructor() {
//  Se crea un objeto Materia con todos sus atributos        
        Timestamp fecha = new Timestamp(0L);
        Materia m = new Materia(4, "Calculo", 3, fecha);
//  Se valida que los atributos hayan sido correctamente asignados
        assertEquals(4, m.getId());
        assertEquals("Calculo", m.getName());
        assertEquals(3, m.getCredits());
        assertSame(fecha, m.getCreatedAt());
    }

    @Test
    @DisplayName("La fecha de creacion puede ser nula (materia aun no guardada)")
    void fechaNula() {
        //  Se crea un objeto Materia sin fecha de creación, simulando un registro nuevo
        Materia m = new Materia(0, "Fisica", 2, null);

        assertNull(m.getCreatedAt());
    }

    @Test
    @DisplayName("toString muestra el nombre de la materia")
    void textoDelObjeto() {
        //  Se crea un objeto Materia para verificar el formato de su representación textual
        Materia m = new Materia(1, "Algebra", 4, null);
//  Se valida que el método toString() retorne el formato esperado
        assertEquals("Materia [name=Algebra]", m.toString());
    }
}
